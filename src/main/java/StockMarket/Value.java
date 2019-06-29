package StockMarket;

public class Value {
    private int valueIdentifier;
    private String name;
    private String ownerValue;
    private String description;
    private long availableQuantity;
    private long budgetValue;

    public Value(int valueIdentifier,String name, String ownerValue, String description, long availableQuantity, long budgetValue) {
        this.valueIdentifier = valueIdentifier;
        this.name = name;
        this.ownerValue = ownerValue;
        this.description = description;
        this.availableQuantity = availableQuantity;
        this.budgetValue = budgetValue;
    }

    public int getValueIdentifier() {
        return valueIdentifier;
    }

    public void setValueIdentifier(int valueIdentifier) {
        this.valueIdentifier = valueIdentifier;
    }

    public long getBudgetValue() {
        return budgetValue;
    }

    public void setBudgetValue(long budgetValue) {
        this.budgetValue = budgetValue;
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

    public long getavailableQuantity() {
        return availableQuantity;
    }

    public void setavailableQuantity(long availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public boolean available (){ return this.availableQuantity != 0;}

    public void decrementQuantity() {this.availableQuantity--;}
}
