package StockMarket;

import java.util.ArrayList;
import java.util.List;

public class ExchangeImpl {
    private List<Value> catalogValues;
    private List<Operation> queuedOperations;
    private List<Operation> processedOperations;

    public ExchangeImpl(List<Value> catalogValues, List<Operation> queuedOperations, List<Operation> processedOperations) {
        this.catalogValues = catalogValues;
        this.queuedOperations = queuedOperations;
        this.processedOperations = processedOperations;
    }

    public ExchangeImpl(List<Value> catalogValues) {
        this.catalogValues = catalogValues;
        this.queuedOperations = new ArrayList<>();
        this.processedOperations = new ArrayList<>();
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
}
