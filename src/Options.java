import java.util.Scanner;
import java.sql.*;

public class Options extends User {
    public String email;

    public Options(String email, Scanner scanner, Connection connection) {
        super(email, scanner, connection);
        this.email = email;
    }

    public void options() throws SQLException {
        System.out.println("Que souhaitez vous faire ?");
        System.out.println("1. Creer salle de vente ");
        System.out.println("2. Acheter ");
        System.out.println("3. Consulter les resultats");
        System.out.println("4. Quitter");
        System.out.println("Votre choix :");

        
        switch (scanner.nextLine()) {
            case "1":
                Tools.clearScreen();
                CreerSalle creerSalle = new CreerSalle(this.email, scanner, connection);
                creerSalle.creerSalle();
                break;
            case "2":
                Tools.clearScreen();
                Acheter acheter = new Acheter(this.email, scanner, connection);
                acheter.acheter();
                break;
            case "3":
                Tools.clearScreen();
                ConsulterResultats consulterResultats = new ConsulterResultats(this.email, scanner, connection);
                consulterResultats.consulterResultats();
                break;    
            case "4":
                Tools.clearScreen();
                quit();
                break;
            default:
                System.out.println("Choix invalide, reessayez");
                Tools.clearScreen();
                options();
                break;
        }
        
    }
}
