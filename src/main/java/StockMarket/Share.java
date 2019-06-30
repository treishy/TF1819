package StockMarket;

import java.text.spi.DateFormatProvider;
import java.util.Date;

public class Share {
    private int quantity;
    private Date boughtDate;
    private Date soldDate;
    private long spendBudget;
    private int valueReference;

    public Share(int quantity, Date boughtDate, long spendBudget, int valueReference) {
        this.quantity = quantity;
        this.boughtDate = boughtDate;
        this.valueReference = valueReference;
        this.spendBudget = spendBudget;
        this.soldDate = null;
    }

    public Share(int quantity, Date boughtDate, Date soldDate, long spendBudget, int valueReference) {
        this.quantity = quantity;
        this.boughtDate = boughtDate;
        this.soldDate = soldDate;
        this.spendBudget = spendBudget;
        this.valueReference = valueReference;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void incrementQuantity(){this.quantity++;}

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

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Identificador do Valor: " + this.valueReference);
        sb.append("Esta accao foi comprada no dia: " + this.boughtDate);
        if(this.soldDate != null)
            sb.append("Esta accao foi vendida no dia: " + this.soldDate);
        sb.append("Esta accao custou: " + this.spendBudget);
        sb.append("Tem " + this.quantity + " accoes deste valor\n");
        return sb.toString();
    }


}
