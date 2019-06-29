package StockMarket;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import spread.*;

import java.util.*;

public class UserStub {
    private User user;
    private List<Long> operationsHistory;

    private static int port;

    private ExchangeImpl exchange = new ExchangeImpl( new HashMap<>());

    private SpreadConnection connection = new SpreadConnection();

    private SpreadGroup group = new SpreadGroup();

    private long counterId = 0;


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
            .build();

    public UserStub(int port, String username) {
        this.port = port;
        this.user = new User(username,0);
        this.operationsHistory = new ArrayList<>();
        //this.exchange.getCatalogValues().put(0,( new Value(0, "TF", "TF", "TF", 100, 40) ));
        System.out.println(serializer.toString());
        connection.add(new BasicMessageListener() {
            @Override
            public void messageReceived(SpreadMessage spreadMessage) {
                try {
                    User user = null;
                    Response rep= null;
                    System.out.println("message received");
                    switch (spreadMessage.getType()){
                        case 1:
                            rep = (Response) spreadMessage.getObject();
                            checkOperationTypeBuy(rep.identifier, rep.valueID, rep.budget);
                        case 2:
                            rep = (Response) spreadMessage.getObject();
                            checkOperationTypeSell(rep.identifier, rep.valueID);
                        case 3:
                            user = (User) spreadMessage.getObject();
                            updateUserWith(user);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    //
    public void updateUserWith(User u){
        this.user.setBudget(u.getBudget());
        this.user.setOwnedShares(u.getOwnedShares());
        this.user.setSharesHistory(u.getSharesHistory());
    }

    public void sendOperationMessage(int value, boolean isBuyOpertion){
        SpreadMessage message = new SpreadMessage();
        try {
            message.setObject(value);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
        message.addGroup("excServers");
        message.setReliable();
        message.setAgreed();
        if (isBuyOpertion)
            message.setType((short) 1);
        else
            message.setType((short) 2);
        try {
            connection.multicast(message);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }




    public void checkOperationTypeBuy(long operationID, int value, long budget){ //tirar budget
        if(!this.operationsHistory.contains(operationID)){
            user.addOneNewShare(value,budget);
        }
    }

    public void checkOperationTypeSell(long operationID, int value){
        if(!this.operationsHistory.contains(operationID)){
            user.removeOneShareByID(value);
        }
    }

    public void init() throws Exception {
        connection.connect(null, 4803,"s"+this.port, false, true);
        //group.join(connection, "excServers");

    }


    public SpreadConnection getConnection() {
        return this.connection;
    }


    public SpreadGroup getPublicGroup() {
        return this.group;
    }

    public Serializer getSerializer() {
        return this.serializer;
    }



    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduza porta a usar");
        int port = scanner.nextInt();
        System.out.println("Introduza seu nome de Utilizador");
        String username = scanner.next();
        UserStub user = new UserStub(port, username);
        try {
            user.init();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        scanner.nextInt();
    }



}
