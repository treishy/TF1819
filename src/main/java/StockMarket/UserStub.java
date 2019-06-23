package StockMarket;

import java.util.List;
import java.util.Map;

public class UserStub {
    User user;

    public UserStub(String username, long budget, Map<Integer, Share> ownedShares , List<Share> sharesHistory) {
        this.user = new User(username,budget,ownedShares,sharesHistory);
    }


}
