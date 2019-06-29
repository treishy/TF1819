package StockMarket;

public class Response {
    //no type vem 1 -> conseguiu compra de valor 2 - > vendeu valor
    public int valueID;
    public long identifier;

    public Response(int valueID, long identifier) {
        this.valueID = valueID;
        this.identifier = identifier;
    }
}
