import java.sql.*;
import java.util.*;


public class Main {


    public static void main(String[] args) throws SQLException {
        Tools.clearScreen();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Bienvenue dans notre application Baie-electronique");
        System.out.println("1. Se connecter");
        System.out.println("2. Quitter");
        System.out.println("Votre choix : ");


        try {
            Connection connection = DatabaseConnection.getConnection();
            User user = new User(null, scanner, connection);
            switch (scanner.nextLine()) {
                case "1":
                    Tools.clearScreen();
                    user.connecter();
                    break;
                case "2":
                    Tools.clearScreen();
                    System.out.println("\n ------------------------- \n");
                    System.out.println("\n Au revoir ! \n\n");
                    connection.close();
                    return;
                default:
                    Tools.clearScreen();
                    System.out.println("Choix invalide, reessayez");
                    main(args);
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
