package StockMarket;

import java.io.Serializable;

public class Response implements Serializable {
    //no type vem 1 -> conseguiu compra de valor 2 - > vendeu valor
    public int valueID;
    public long identifier;
    public String userId;
    public long budget;

    public Response(String userId, int valueID, long identifier, long budget) {
        this.userId = userId;
        this.valueID = valueID;
        this.identifier = identifier;
        this.budget = budget;
    }

    public String getUserID () {
        return this.userId;
    }
}
