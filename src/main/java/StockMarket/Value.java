package StockMarket;

public class Value {
    private int valueIdentifier;
    private String name;
    private String ownerValue;
    private String description;
    private long totalQuantity;

    public Value(int valueIdentifier,String name, String ownerValue, String description, long totalQuantity) {
        this.valueIdentifier = valueIdentifier;
        this.name = name;
        this.ownerValue = ownerValue;
        this.description = description;
        this.totalQuantity = totalQuantity;
    }

    public int getValueIdentifier() {
        return valueIdentifier;
    }

    public void setValueIdentifier(int valueIdentifier) {
        this.valueIdentifier = valueIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerValue() {
        return ownerValue;
    }

    public void setOwnerValue(String ownerValue) {
        this.ownerValue = ownerValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
