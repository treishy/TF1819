package StockMarket;

import java.io.Serializable;
import java.util.*;

public class User implements Serializable {
    private String username;
    private long budget;
    private Map<Integer , Share> ownedShares;
    private List<Share> sharesHistory;

    public User(String username, long budget, Map<Integer, Share> ownedShares ,List<Share> sharesHistory) {
        this.username = username;
        this.budget = budget;
        this.ownedShares = ownedShares;
        this.sharesHistory = sharesHistory;
    }

    public User(String username, long budget) {
        this.username = username;
        this.budget = budget;
        this.ownedShares = new HashMap<>();
        this.sharesHistory = new ArrayList<>();
    }


    public Map<Integer, Share> getOwnedShares() {
        return ownedShares;
    }

    public void setOwnedShares(Map<Integer, Share> ownedShares) {
        this.ownedShares = ownedShares;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public long getBudget() {
        return budget;
    }

    public void setBudget(long budget) {
        this.budget = budget;
    }

    public List<Share> getSharesHistory() {
        return sharesHistory;
    }

    public void setSharesHistory(List<Share> sharesHistory) {
        this.sharesHistory = sharesHistory;
    }

    public boolean insertBuyOperationIsValid(long budget){
        if((this.budget - budget) >= 0){
            this.budget -= budget;
            return true;
        }
        else return false;
    }

    public void removeOneShareByID(int valueID){
        if(ownedShares.containsKey(valueID)) {
            Share share = ownedShares.get(valueID);
                if (share.getQuantity() > 1) {
                    share.setQuantity(share.getQuantity() - 1);
                }
                else ownedShares.remove(valueID);
                this.sharesHistory.add(new Share(1, share.getBoughtDate(), new Date(), share.getSpendBudget(), share.getValueReference()));
                this.budget += share.getSpendBudget();
        }
    }

    public void addOneNewShare(int valueID, long budget){

        if (ownedShares.containsKey(valueID)){
            this.ownedShares.get(valueID).incrementQuantity();
        }
        else this.ownedShares.put(valueID,(new Share(1,new Date(),budget,valueID)));

    }

    public void changeBudget(long budget){
        this.budget+=budget;
    }
}
