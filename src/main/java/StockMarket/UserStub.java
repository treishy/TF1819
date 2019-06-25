package StockMarket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserStub {
    User user;
    List<Long> operationsHistory;

    //
    public UserStub(String username, long budget) { //usar este apenas depois de obter saldo apos contactar Exchange por budget free p.e.
        this.user = new User(username,budget);
        this.operationsHistory = new ArrayList<>();
    }

    public void checkOperationTypeBuy(long operationID, int value, long budget){ //tirar budget
        if(!this.operationsHistory.contains(operationID)){
            user.addOneNewShare(value,budget);
        }
    }

    public void checkOperationTypeSell(long operationID, int value){
        if(!this.operationsHistory.contains(operationID)){
            user.removeOneShareByID(value);
        }
    }



}
