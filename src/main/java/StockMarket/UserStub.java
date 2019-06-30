package StockMarket;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import spread.*;

import java.net.InetAddress;
import java.util.*;

public class UserStub {
    private User user;
    private List<Long> operationsHistory;

    private static int port;

    private ExchangeImpl exchange = new ExchangeImpl( new HashMap<>());

    private SpreadConnection connection = new SpreadConnection();

    private SpreadGroup group = new SpreadGroup();

    private long counterId = 0;

    public User getUser() {
        return user;
    }

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
        try {
            connection.connect(InetAddress.getByName("localhost"),0, username, false,false);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                            break;
                        case 2:
                            rep = (Response) spreadMessage.getObject();
                            checkOperationTypeSell(rep.identifier, rep.valueID);
                            break;
                        case 3:
                            user = (User) spreadMessage.getObject();
                            updateUserWith(user);
                            break;
                        default:
                            System.out.print("Not ready for this kind of message");
                            break;
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

    public void sendOperationMessage(int valueID, boolean isBuyOpertion){
        SpreadMessage message = new SpreadMessage();
        try {
            Request req = new Request(this.user.getUsername(),valueID);
            message.setObject(req);
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

    public void sendUserUpdateMessage(String username){
        SpreadMessage message = new SpreadMessage();
        try {
            message.setObject(username);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
        message.addGroup("excServers");
        message.setReliable();
        message.setAgreed();
        message.setType((short) 3);
        try {
            connection.multicast(message);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }


    public void checkOperationTypeBuy(long operationID, int value, long budget){
        if(!this.operationsHistory.contains(operationID)){
            user.addOneNewShare(value,budget);
        }
    }

    public void checkOperationTypeSell(long operationID, int value){
        if(!this.operationsHistory.contains(operationID)){
            user.removeOneShareByID(value);
        }
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





}
