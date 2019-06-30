package StockMarket;

import java.util.Scanner;

public class Client {

    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduza porta a usar");
        int port = scanner.nextInt();
        System.out.println("Introduza seu nome de Utilizador");
        String username = scanner.next();
        UserStub user = new UserStub(port, username);
        //interfaceCliente(scanner);
        int option=1;
        do {
            System.out.println("Introduza o tipo de operacao a fazer\n 0- Logout\n 1- Compra de Valor\n 2- Venda de Valor\n 3- Consulta de Valores que possui");
            int valueID;
            option = scanner.nextInt();
            switch (option ) {
                case 1:
                    System.out.println("Introduza o valor para o qual deseja fazer a compra");
                    valueID = scanner.nextInt();
                    if(user.getUser().insertBuyOperationIsValid(user.obtainValueLong(valueID)))
                        user.sendOperationMessage(valueID, true);
                    else
                        System.out.println("Nao tem orcamento para comprar esse valor, o seu saldo atual e " + user.getUser().getBudget());

                    break;
                case 2:
                    System.out.println("Introduza o valor para o qual deseja fazer a venda");
                    valueID = scanner.nextInt();
                    user.sendOperationMessage(valueID, false);
                    break;
                case 3:
                    System.out.println(user.getUser().getOwnedShares().toString());
                    break;
                case 0:
                    System.out.println("Logging out!");
                    break;
            }
        } while(option != 0);

    }

}
