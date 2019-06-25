package StockMarket;

public class Request {
    public String userID;
    public int valueID;
    public int quantity;

    public Request(String userID, int valueID, int quantity) {
        this.userID = userID;
        this.valueID = valueID;
        this.quantity = quantity;
    }
}
