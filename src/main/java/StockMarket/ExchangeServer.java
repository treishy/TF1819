package StockMarket;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.io.Serializable;
import java.util.*;

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
            .addType(Response.class)
            .build();

    public ExchangeServer(int port) {
        this.port = port;
        this.recovery = new ExchangeRecovery<>( this );
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
                            processRequest(buffered, serializer.decode( buffered.getData() ));
                        }

                        recovery.getQueue().clear();

                        processRequest(message, obj);
                    }
                }
            } catch ( SpreadException e ) {
                e.printStackTrace();
            }
        } );
    }

    private void processRequest(SpreadMessage spreadMessage, Object obj) throws SpreadException {
        Request request;
        Operation operation;

        switch (spreadMessage.getType()) {
            case 1:
                System.out.printf("Received a new buy operation ...\n");
                request = (Request)obj;
                operation = new Operation(request.getValueID(), request.getUserID(), counterId, true);
                exchange.addNewOrder(operation);
                processChanges(exchange.processOperation());
                counterId++;
                break;
            case 2:
                System.out.printf("Received a new sell operation ...\n");
                request = (Request)obj;
                operation = new Operation(request.getValueID(), request.getUserID(), counterId, false);
                exchange.addNewOrder(operation);
                processChanges(exchange.processOperation());
                counterId++;
                break;
            case 3:
                System.out.println("Received a new user update message ...");
                String username = (String) obj;
                User user = exchange.getUsers().get(username);
                sendResponse(user, (short) 3);
                break;
            default:
                System.out.printf("Received a unknown request ...\n");
        }
    }

    private void processChanges(Map<Operation, Long> changes) throws SpreadException {
        for (Map.Entry<Operation, Long> entry : changes.entrySet()) {
            Operation operation = entry.getKey();
            Response response = new Response(operation.getValueID(), operation.getIdentifier(), entry.getValue());

            if (operation.isBuyOperation())
                sendResponse(response, (short) 1);
            else
                sendResponse(response, (short) 2);
        }
    }

    private void sendResponse(Object obj, short type) throws SpreadException {
        SpreadMessage spreadMessage = new SpreadMessage();
        spreadMessage.setData(this.serializer.encode(obj));
        spreadMessage.setType(type);
        spreadMessage.setReliable();
        connection.multicast(spreadMessage);
    }

    public void init() throws Exception {
        initCatalogValues();
        initUsers();
        connection.connect(null, 4803,"s"+this.port, false, true);
        group.join(connection, "excServers");
        connectionHandler();
    }

    private void initCatalogValues() {
        Map<Integer, Value> catalogValues = new HashMap<>();
        catalogValues.put(0,( new Value(0, "TF", "TF", "TF", 3, 40)));
        catalogValues.put(1,( new Value(1, "Petroleo", "Galp", "Petroleo Da Galp", 5, 100)));
        catalogValues.put(2,( new Value(2, "Apple", "Apple Inc.", "Empresa Multinacional Americana", 100, 70)));
        catalogValues.put(3,( new Value(3, "BitCoin", "Satoshi Nakamoto", "Cryptocurrency", 10, 150)));
        catalogValues.put(4,( new Value(4, "Ouro", "Unknown", "Elemento Quimico de Alto Valor", 30, 40)));
        catalogValues.put(5,( new Value(5, "Platina", "Unknown", "Elemento Quimico de Mediano Valor", 30, 30)));
        exchange.setCatalogValues(catalogValues);
    }

    private void initUsers() {
        Map<String, User> users = new HashMap<>();
        users.put("johnsnow", (new User("jonsnow", 500)));
        users.put("nightking", (new User("nightking", 500)));
        exchange.setUsers(users);
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