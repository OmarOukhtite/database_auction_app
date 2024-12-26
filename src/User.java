import java.util.Scanner;
import java.sql.*;

public class User {
    public String email;
    public Scanner scanner;
    public Connection connection;

    public User(String email, Scanner scanner, Connection connection) {
        this.email = email;
        this.scanner = scanner;
        this.connection = connection;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void quit() {
        try {
            System.out.println("\n ------------------------- \n");
            System.out.println("Deconnexion en cours...");
            System.out.println("\n ------------------------- \n");
            System.out.println("Au revoir ! \n\n");
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // connecter l'utilisateur
    public void connecter() throws SQLException {
        System.out.println("Entrez votre adresse mail : (ou tapez 'annuler')");
        String email = scanner.nextLine();
        User user = new User(email, scanner, connection);
        if (email.equals("annuler")) {
            Main.main(null);
            return;
        }
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Utilisateur WHERE email = '" + email + "'");
            // verifier si l'utilisateur existe
            if (resultSet.next()) {
                Tools.clearScreen();
                System.out.println("Bienvenue " + resultSet.getString("prenom") + " " + resultSet.getString("nom") + " \n");
                Options optionsInstance = new Options(user.getEmail(), scanner, connection);
                optionsInstance.options();
            } else {
                Tools.clearScreen();
                System.out.println("Reessayez.");
                connecter();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}