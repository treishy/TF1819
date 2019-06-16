package StockMarket;

import java.text.spi.DateFormatProvider;
import java.util.Date;

public class Share {
    private int quantity;
    private Date boughtDate;
    private Date soldDate;
    private long spendBudget;
    private int valueReference;

    public Share(int quantity, Date boughtDate, Date soldDate, int valueReference) {
        this.quantity = quantity;
        this.boughtDate = boughtDate;
        this.soldDate = soldDate;
        this.valueReference = valueReference;
        this.soldDate = null;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getBoughtDate() {
        return boughtDate;
    }

    public void setBoughtDate(Date boughtDate) {
        this.boughtDate = boughtDate;
    }

    public Date getSoldDate() {
        return soldDate;
    }

    public void setSoldDate(Date soldDate) {
        this.soldDate = soldDate;
    }

    public long getSpendBudget() {
        return spendBudget;
    }

    public void setSpendBudget(long spendBudget) {
        this.spendBudget = spendBudget;
    }

    public int getValueReference() {
        return valueReference;
    }

    public void setValueReference(int valueReference) {
        this.valueReference = valueReference;
    }

    public void finishShare(){
        this.soldDate = new Date();
    }

}
