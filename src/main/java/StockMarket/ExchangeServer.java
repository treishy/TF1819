package StockMarket;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

class State {

    long counterId;
    ExchangeImpl exchange;

    public State(long counterId, ExchangeImpl exchange) {
        this.counterId = counterId;
        this.exchange = exchange;
    }

    @Override
    public String toString() {
        return counterId + "\n";
    }
}

public class ExchangeServer implements Stateful<State> {

    private static int port;

    private ExchangeImpl exchange = new ExchangeImpl( new HashMap<>());

    private SpreadConnection connection = new SpreadConnection();

    private SpreadGroup group = new SpreadGroup();

    private long counterId = 0;

    protected ExchangeRecovery<State> recovery;

    private Serializer serializer = new SerializerBuilder()
            .addType(Value.class)
            .addType(Operation.class)
            .addType(Operation.State.class)
            .addType(User.class)
            .addType(Share.class)
            .addType(Date.class)
            .addType(State.class)
            .addType(ExchangeImpl.class)
            .addType(StateRequest.class)
            .addType(StateResponse.class)
            .addType(ExchangeServer.class)
            .addType(Request.class)
            .build();

    public ExchangeServer(int port) {
        this.port = port;
        this.recovery = new ExchangeRecovery<>( this );
        this.exchange.getCatalogValues().put(0,( new Value(0, "TF", "TF", "TF", 100, 40) ));
        System.out.println(serializer.toString());
    }

    private void connectionHandler() {
        connection.add( message -> {
            try {
                if ( recovery.onMembershipMiddleware( message ) ) return;

                if ( message.isRegular() ) {
                    Object obj = serializer.decode( message.getData() );

                    if ( recovery.onStateRequestMiddleware( message, obj ) ) return;
                    if ( recovery.onStateResponseMiddleware( message, obj ) ) return;
                    if ( recovery.onBufferedMessageMiddleware( message ) ) return;

                    if ( recovery.getIsRecovered() ) {
                        SpreadMessage buffered;

                        while ( ( buffered = recovery.getQueue().poll() ) != null ) {
                            processRequest(buffered);
                        }

                        recovery.getQueue().clear();

                        processRequest(message);
                    }
                }
            } catch ( SpreadException e ) {
                e.printStackTrace();
            }
        } );
    }

    private void processRequest(SpreadMessage spreadMessage) throws SpreadException {
        Request request = (Request) spreadMessage.getObject();
        Operation operation;
        HashMap<Integer,Long> changes = null;
        switch (spreadMessage.getType()) {
            case 1:
                System.out.printf("Received new buy operation ...\n");
                operation = new Operation(request.getValueID(), request.getUserID(), counterId, true);
                exchange.addNewOrder(operation);
                changes = exchange.processOperation();
                //processchanges
                counterId++;
                break;
            case 2:
                System.out.printf("Received new sell operation ...\n");
                operation = new Operation(request.getValueID(), request.getUserID(), counterId, false);
                exchange.addNewOrder(operation);
                changes = exchange.processOperation();
                counterId++;
                //process changes
                break;
            default:
                System.out.printf("Received a unknown request ...\n");
        }
    }

    public void init() throws Exception {
        connection.connect(null, 4803,"s"+this.port, false, false);
        group.join(connection, "excServers");
        connectionHandler();
    }

    @Override
    public SpreadConnection getConnection() {
        return this.connection;
    }

    @Override
    public SpreadGroup getPublicGroup() {
        return this.group;
    }

    @Override
    public Serializer getSerializer() {
        return this.serializer;
    }

    @Override
    public State getState() {
        System.out.printf("Preparing state to transfer ...\n");
        return new State(this.counterId, this.exchange);
    }

    @Override
    public void setState(State state) {
        System.out.printf("Setting state ...\n");
        this.counterId = state.counterId;
        this.exchange = state.exchange;
        System.out.println(state.toString());
    }

    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        ExchangeServer server = new ExchangeServer(scanner.nextInt());
        try {
            server.init();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        scanner.nextInt();
    }
}