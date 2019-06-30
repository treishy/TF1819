package StockMarket;

import java.io.Serializable;

public class Request implements Serializable {

    public String userID;

    public int valueID;

    public Request(String userID, int valueID) {
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
