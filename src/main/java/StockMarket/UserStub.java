package StockMarket;

import java.util.List;

public class UserStub {
    private String username;
    private String password;
    private long budget;
    private List<Share> ownedShares;
    private List<Share> sharesHistory;

    public UserStub(String username, String password, long budget, List<Share> ownedShares ,List<Share> sharesHistory) {
        this.username = username;
        this.password = password;
        this.budget = budget;
        this.ownedShares = ownedShares;
        this.sharesHistory = sharesHistory;
    }


    public List<Share> getOwnedShares() {
        return ownedShares;
    }

    public void setOwnedShares(List<Share> ownedShares) {
        this.ownedShares = ownedShares;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
