package StockMarket;

public class Operation {
    private long identifier;
    private boolean buyOperation; //true = compra false = venda
    private int state ; //1 - por processar //2 - processada e a espera de conclusao //3 concluida //0 cancelada

    public Operation(long identifier, boolean buyOperation, int state) {
        this.identifier = identifier;
        this.buyOperation = buyOperation;
        this.state = state;
    }


    public long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    public boolean isBuyOperation() {
        return buyOperation;
    }

    public void setBuyOperation(boolean buyOperation) {
        this.buyOperation = buyOperation;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void evolveState(){
        if(this.state != 0 && this.state !=4) this.state++;
    }

    //possivelmente metodos para ver se esta cancelada, por processar, processada e concluida para nao ficar no codigo posterior
}
