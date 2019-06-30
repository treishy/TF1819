package StockMarket;

import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import spread.*;

import java.net.InetAddress;
import java.util.*;

public class UserStub {
    private User user;
    private List<Long> operationsHistory;
    private Map<Integer,Value> catalogValues= new HashMap<>();
    private static int port;
    private boolean updated= false;

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
            .addType(Request.class)
            .addType(Response.class)
            .build();

    public UserStub(int port, String username) {
        this.port = port;
        this.user = new User(username,0);
        this.operationsHistory = new ArrayList<>();
        //this.exchange.getCatalogValues().put(0,( new Value(0, "TF", "TF", "TF", 100, 40) ));
        System.out.println(serializer.toString());
        try {
            connection.connect(InetAddress.getByName("localhost"),0, username, false,false);
            group.join(connection, user.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
        initCatalogValues();
        connection.add( spreadMessage -> {
            try {
                User user = null;
                Response rep= null;
                System.out.println("message received");
                switch (spreadMessage.getType()){
                    case 1:
                        System.out.println("Recebido Confirmacao de Buy");
                        rep = this.serializer.decode(spreadMessage.getData());
                        checkOperationTypeBuy(rep.identifier, rep.valueID, rep.budget);
                        break;
                    case 2:
                        System.out.println("Recebido Confirmacao de Share Vendida");
                        rep = this.serializer.decode(spreadMessage.getData());
                        checkOperationTypeSell(rep.identifier, rep.valueID);
                        break;
                    case 3:
                        System.out.println("Recebido Informacao de Utilizador");
                        if(!updated) {
                            user = this.serializer.decode(spreadMessage.getData());
                            updateUserWith(user);
                        }
                        break;
                    default:
                        System.out.print("Not ready for this kind of message");
                        break;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        } );
        sendUserUpdateMessage(username);

    }

    public void initCatalogValues(){
        this.catalogValues.put(0,( new Value(0, "TF", "TF", "TF", 3, 40)));
        this.catalogValues.put(1,( new Value(1, "Petroleo", "Galp", "Petroleo Da Galp", 5, 100)));
        this.catalogValues.put(2,( new Value(2, "Apple", "Apple Inc.", "Empresa Multinacional Americana", 100, 70)));
        this.catalogValues.put(3,( new Value(3, "BitCoin", "Satoshi Nakamoto", "Cryptocurrency", 10, 150)));
        this.catalogValues.put(4,( new Value(4, "Ouro", "Unknown", "Elemento Quimico de Alto Valor", 30, 40)));
        this.catalogValues.put(5,( new Value(5, "Platina", "Unknown", "Elemento Quimico de Mediano Valor", 30, 30)));

    }
    //
    public void updateUserWith(User u){
            this.user.setBudget(u.getBudget());
            this.user.setOwnedShares(u.getOwnedShares());
            this.user.setSharesHistory(u.getSharesHistory());
            this.updated = true;

    }

    public void sendOperationMessage(int valueID, boolean isBuyOpertion){
        SpreadMessage message = new SpreadMessage();
        Request req = new Request(this.user.getUsername(),valueID);
        message.setData(this.serializer.encode(req));
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
        message.setData(this.serializer.encode(username));
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
            this.operationsHistory.add(operationID);
            user.addOneNewShare(value,budget);
            System.out.println("You Managed to Get the Share for the Value " + value);
        }
    }

    public void checkOperationTypeSell(long operationID, int value){
        if(!this.operationsHistory.contains(operationID)){
            this.operationsHistory.add(operationID);
            user.removeOneShareByID(value);
            System.out.println("You Managed to Get the Share for the Value " + value);
        }
    }

    public long obtainValueLong(int valueId){
        if(this.catalogValues.containsKey(valueId)){
            return this.catalogValues.get(valueId).getBudgetValue();
        }
        else return 0;

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
