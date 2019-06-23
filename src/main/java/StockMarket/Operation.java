package StockMarket;

public class Operation {
    private String userID;
    private long identifier;
    private int valueID;
    private boolean buyOperation; //true = compra false = venda
    private State state ; //1 - por processar //2 - processada e a espera de conclusao //3 concluida //0 cancelada
    enum State{
        CANCELLED,
        TOPROCESS,
        PROCESSED,
        CONCLUDED
    }

    public Operation(int valueID,String userID, long identifier, boolean buyOperation) {
        this.userID = userID;
        this.valueID = valueID;
        this.identifier = identifier;
        this.buyOperation = buyOperation;
        this.state = State.TOPROCESS;
    }

    public int getValueID() {
        return valueID;
    }

    public void setValueID(int valueID) {
        this.valueID = valueID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void cancelOperation(){ this.state = State.CANCELLED; }

    public boolean evolveState(){
        boolean stateEvolved = true;
        switch(this.state) {
            case TOPROCESS:
                this.state = State.PROCESSED;
                break;
            case PROCESSED:
                this.state = State.CONCLUDED;
                break;
            default:
                stateEvolved = false;
                break;
        }
        return stateEvolved;
    }

    //possivelmente metodos para ver se esta cancelada, por processar, processada e concluida para nao ficar no codigo posterior
}
