package StockMarket;

public class Response {
    //no type vem 0 -> conseguiu compra de valor 1 - > vendeu valor
    public int valueID;
    public long budgetSpent;
    public long identifier;

    public Response(int valueID, long budgetSpent, long identifier) {
        this.valueID = valueID;
        this.budgetSpent = budgetSpent;
        this.identifier = identifier;
    }
}
