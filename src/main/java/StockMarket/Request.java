package StockMarket;

public class Request {

    public String userID;

    public int valueID;

    public Request(String userID, int valueID, int quantity) {
        this.userID = userID;
        this.valueID = valueID;
    }

    public String getUserID() {
        return userID;
    }

    public int getValueID() {
        return valueID;
    }
}
