package StockMarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static StockMarket.Operation.State.TOPROCESS;

public class ExchangeImpl {
    private List<Value> catalogValues;
    private List<Operation> queuedOperations;
    private List<Operation> processedOperations;
    private Map<String,User> users;

    public ExchangeImpl(List<Value> catalogValues, List<Operation> queuedOperations, List<Operation> processedOperations) {
        this.catalogValues = catalogValues;
        this.queuedOperations = queuedOperations;
        this.processedOperations = processedOperations;
    }

    public ExchangeImpl(List<Value> catalogValues) {
        this.catalogValues = catalogValues;
        this.queuedOperations = new ArrayList<>();
        this.processedOperations = new ArrayList<>();
        this.users = new HashMap<>();
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public List<Value> getCatalogValues() {
        return catalogValues;
    }

    public void setCatalogValues(List<Value> catalogValues) {
        this.catalogValues = catalogValues;
    }

    public List<Operation> getQueuedOperations() {
        return queuedOperations;
    }

    public void setQueuedOperations(List<Operation> queuedOperations) {
        this.queuedOperations = queuedOperations;
    }

    public List<Operation> getProcessedOperations() {
        return processedOperations;
    }

    public void setProcessedOperations(List<Operation> processedOperations) {
        this.processedOperations = processedOperations;
    }

    public boolean addNewOrder(Operation op){
        boolean operationAdded = true;
        if(op.getState() == TOPROCESS)
            this.queuedOperations.add(op);
        else operationAdded = false;
        return operationAdded;
    }

    public void processOperation(){ //secalhar meter a devolver um map {id do utilizador, mudanca de valor}
        Operation op = null;
        if(this.queuedOperations.size() > 0) {
            op = this.queuedOperations.get(0);
            this.queuedOperations.remove(op);
            boolean found = false;
            for(Operation pop: this.processedOperations){
                if (op.getValueID()==(pop.getValueID()) && op.isBuyOperation() != op.isBuyOperation()){
                    found = true;
                    pop.evolveState();
                    op.evolveState();
                    this.processedOperations.remove(pop);
                    found= true;
                    if (pop.isBuyOperation()) {
                        removeUserShare(op.getValueID(),op.getUserID());
                        addUserShare(pop.getValueID(),pop.getUserID());
                    }
                    else {
                        removeUserShare(pop.getValueID(),pop.getUserID());
                        addUserShare(op.getValueID(),op.getUserID());
                    }
                    break;
                }
            }
            if(!found)
                this.processedOperations.add(op);
        }

    }

    public void removeUserShare(int valueID, String user){
        this.users.get(user).removeOneShareByID(valueID);
    }

    public void addUserShare(int valueID, String user){
        this.users.get(user).addOneNewShare(valueID, 44); //obter budget de algum lado xd
    }
}
