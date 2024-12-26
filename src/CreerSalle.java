import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class CreerSalle extends Options {

    public CreerSalle(String email, Scanner scanner, Connection connection) {
        super(email, scanner, connection);
    }

    /**
     * Permet à l'utilisateur de choisir une catégorie parmi celles disponibles dans la base de données.
     * Affiche les catégories disponibles et vérifie la validité de l'entrée utilisateur.
     * 
     * @return le nom de la catégorie choisie, ou "None" si l'utilisateur annule.
     */
    public String choixCategorie() throws SQLException {
        ResultSet resultSet;
        System.out.println("Categories disponibles : ");
        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT * FROM Categorie");
        while (resultSet.next()) {
            System.out.println(resultSet.getString("NOMCATEGORIE") + " : " + resultSet.getString("DESCRIPTION"));
        }
    
        String categorie = null;
        boolean categorieValide = false;
        
        // Demander la categorie a l'utilisateur 
        while (!categorieValide) {
            System.out.println("Choisissez une categorie : (ou tapez 'annuler')");
            categorie = scanner.nextLine();
    
            if (categorie.equalsIgnoreCase("annuler")) {
                Tools.clearScreen();
                options();
                return "None";
            }
    
            String query = "SELECT * FROM Categorie WHERE NOMCATEGORIE = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, categorie);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                categorieValide = true;
            } else {
                System.out.println("Categorie invalide! Reessayez");
            }
            preparedStatement.close();
        }
        statement.close();
        resultSet.close();

        return categorie;
    }

    /**
     * Crée une salle de vente dans la base de données pour une catégorie choisie par l'utilisateur.
     * Vérifie la disponibilité des produits avant de procéder.
     */
    public void creerSalle() throws SQLException {
        String categorie = choixCategorie();
        if (categorie.equals("None")) return;

        String sqlCheckProduits = "SELECT COUNT(*) FROM Produit p WHERE p.NOMCATEGORIE = ? and STOCK > 0 " +
                "AND p.NUMEROPRODUIT NOT IN (SELECT v.NumeroProduit FROM Vente v)";
        PreparedStatement stmtCheckProduits = connection.prepareStatement(sqlCheckProduits);
        stmtCheckProduits.setString(1, categorie);
        ResultSet rsCheckProduits = stmtCheckProduits.executeQuery();

        int produitsDisponibles = 0;
        if (rsCheckProduits.next()) {
            produitsDisponibles = rsCheckProduits.getInt(1);
        }

        if (produitsDisponibles == 0) {
            System.out.println("\n Aucun produit disponible dans cette catÃ©gorie. Impossible de crÃ©er une salle de vente.\n");
            stmtCheckProduits.close();
            creerSalle();
            return;
        }
        
        // Executer la requete pour obtenir le dernier NumeroSalle
        Statement stmt = connection.createStatement();
        String sqlGetLastNumeroSalle = "SELECT MAX(NumeroSalle) FROM Salle";
        ResultSet rs = stmt.executeQuery(sqlGetLastNumeroSalle);

        int numeroSalle = 0;
        if (rs.next()) {
            numeroSalle = rs.getInt(1) + 1;
        }
        stmt.close();
        rs.close();

        // CrÃ©er la salle de vente 
        String sqlInsertSalle = "INSERT INTO Salle (NumeroSalle, NomCategorie) VALUES (?, ?)";
        PreparedStatement stmtSalle = connection.prepareStatement(sqlInsertSalle);
        stmtSalle.setInt(1, numeroSalle);
        stmtSalle.setString(2, categorie);
        stmtSalle.executeUpdate();

        System.out.println("Salle de vente crÃ©Ã©e avec succÃ¨s ! Salle nÂ°" + numeroSalle);
        ajouteProduit(numeroSalle, categorie);
        options();

        stmtSalle.close();
    }

    /**
     * Ajoute des produits à une salle de vente existante, en fonction de la catégorie choisie.
     * Propose les produits disponibles, permet la configuration des ventes, et gère les options spécifiques.
     * 
     * @param numeroSalle le numéro de la salle de vente à laquelle ajouter des produits.
     * @param categorie la catégorie des produits à ajouter.
     */
    public void ajouteProduit(int numeroSalle, String categorie)throws SQLException{
  
        int numProduit = -1;
        Statement statement = connection.createStatement();
        String sqlSelectProduits = "SELECT * FROM Produit p WHERE p.NOMCATEGORIE = ? and STOCK > 0 "
            + "AND p.NUMEROPRODUIT NOT IN (SELECT v.NumeroProduit FROM Vente v)";
        PreparedStatement stmtSelectProduits = connection.prepareStatement(sqlSelectProduits);
        stmtSelectProduits.setString(1, categorie);
        ResultSet resultSet;

        while (true) {
            // Afficher les produits disponibles dans la categorie
            System.out.println("Produits disponibles dans la catÃ©gorie " + categorie + " :");
            resultSet = stmtSelectProduits.executeQuery();
            
            boolean produitDisponible = false;
            Set<Integer> produitsDisponibles = new HashSet<>();
            while (resultSet.next()) {
                produitDisponible = true;
                int numProduitDisponible = resultSet.getInt("NUMEROPRODUIT");
                System.out.println("ID : " + numProduitDisponible + " - "
                        + resultSet.getString("NOMPRODUIT") + " - Stock : "
                        + resultSet.getInt("STOCK"));
                produitsDisponibles.add(numProduitDisponible);
            }

            if (!produitDisponible) {

                System.out.println("\n Aucun produit disponible dans cette catÃ©gorie.\n");
                break;
            }

            System.out.println("Entrez le numÃ©ro du produit Ã  ajouter Ã  la salle de vente (ou tapez 'annuler') : ");
            String inputProduit = scanner.nextLine();
    

    
            if (inputProduit.equalsIgnoreCase("annuler")) {
                stmtSelectProduits.close();
                resultSet.close();
                statement.close();
                Tools.clearScreen();
                return;
            }
    
            try {
                numProduit = Integer.parseInt(inputProduit);
                // Verifier si le numero de produit est dans les produits disponibles
                if (!produitsDisponibles.contains(numProduit)) {
                    Tools.clearScreen();
                    System.out.println("NumÃ©ro de produit invalide. Veuillez entrer un numÃ©ro parmi les produits disponibles.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un numÃ©ro de produit valide.");
                continue;
            }

            // Ajout le produits selectionne a la vente 
            String sqlGetLastNumeroVente = "SELECT MAX(NumeroVente) FROM Vente";
            resultSet = statement.executeQuery(sqlGetLastNumeroVente);
            int NumeroVente = 0;
            if (resultSet.next()) {
                NumeroVente = resultSet.getInt(1) + 1;
            }


            String sqlInsertVente = "INSERT INTO Vente (NumeroVente, NumeroSalle, NumeroProduit, PrixDepart, Revocable, Montante, PLUSIEURSOFFRESPARUTILISATEUR, DateDebut) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

            PreparedStatement stmtInsertVente = connection.prepareStatement(sqlInsertVente);
            stmtInsertVente.setInt(1, NumeroVente);
            stmtInsertVente.setInt(2, numeroSalle);
            stmtInsertVente.setInt(3, numProduit);

            int prixDepart;
            do {
                System.out.print("Entrez le prix de dÃ©part : ");
                while (!scanner.hasNextInt()) {
                    System.out.print("Veuillez entrer un entier valide : ");
                    scanner.next();
                }
                prixDepart = scanner.nextInt();
            } while (prixDepart < 0);
            stmtInsertVente.setInt(4, prixDepart);

            String revocable;
            do {
                System.out.print("RÃ©vocable (Y/N) : ");
                revocable = scanner.next().toUpperCase();
            } while (!revocable.equals("Y") && !revocable.equals("N"));
            stmtInsertVente.setString(5, revocable);

            String montante;
            do {
                System.out.print("Montante (Y/N) : ");
                montante = scanner.next().toUpperCase();
            } while (!montante.equals("Y") && !montante.equals("N"));
            stmtInsertVente.setString(6, montante);

            String plusieursOffres;
            do {
                System.out.print("Plusieurs offres par utilisateur (Y/N) : ");
                plusieursOffres = scanner.next().toUpperCase(); 
            } while (!plusieursOffres.equals("Y") && !plusieursOffres.equals("N"));
            stmtInsertVente.setString(7, plusieursOffres);
            stmtInsertVente.executeUpdate();



            String dureeLimitee;
            if (montante.equals("Y")){
                do {
                    System.out.print("La vente a-t-elle une durÃ©e limitÃ©e (Oui/Non) ? ");
                    dureeLimitee = scanner.next().toUpperCase();
                } while (!dureeLimitee.equals("OUI") && !dureeLimitee.equals("NON")); 
            } else {
                dureeLimitee = "NON";
            }

            if (dureeLimitee.equalsIgnoreCase("Oui")) {
                // Demander la date de fin
                String dateTimeStr;
                LocalDateTime dateTime;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                scanner.nextLine();

                while (true) {
                    System.out.print("Entrez la date et l'heure (AAAA-MM-JJ HH:mm:ss) : ");
                    dateTimeStr = scanner.nextLine();
                    try {
                        dateTime = LocalDateTime.parse(dateTimeStr, formatter);
            
                        // Vérifier que la date saisie est supérieure à maintenant
                        if (dateTime.isAfter(LocalDateTime.now())) {
                            break;
                        } else {
                            System.out.println("La date et l'heure doivent être dans le futur. Réessayez.");
                        }
                    } catch (DateTimeParseException e) {
                        System.out.println("Format de date et heure invalide. Réessayez (AAAA-MM-JJ HH:mm:ss).");
                    }
                }

                Timestamp dateTimeSql = Timestamp.valueOf(dateTime);

                // Preparer et executer l'insertion SQL
                String sqlInsertLimitee = "INSERT INTO VENTEDUREELIMITEE (NumeroVente, DateFin) VALUES (?, ?)";
                PreparedStatement stmtInsertLimitee = connection.prepareStatement(sqlInsertLimitee);

                stmtInsertLimitee.setInt(1, NumeroVente);
                stmtInsertLimitee.setTimestamp(2, dateTimeSql);
                stmtInsertLimitee.executeUpdate();

                System.out.println("Vente ajoutÃ©e Ã  VENTEDUREELIMITEE avec la date de fin : " + dateTimeStr);
            } else {
                String sqlInsertIllimitee = "INSERT INTO VENTEDUREEILLIMITEE (NumeroVente) VALUES (?)";
                PreparedStatement stmtInsertIllimitee = connection.prepareStatement(sqlInsertIllimitee);
            
                stmtInsertIllimitee.setInt(1, NumeroVente);
                stmtInsertIllimitee.executeUpdate();
            
                System.out.println("Vente ajoutÃ©e Ã  VENTEDUREEILLIMITEE.");
            }

            NumeroVente += 1 ;
            scanner.nextLine();

            System.out.println("Produit nÂ°" + numProduit + " ajoutÃ© Ã  la vente, dans la salle  nÂ°" + numeroSalle );
            stmtInsertVente.close();
        }
        stmtSelectProduits.close();
        resultSet.close();
        statement.close();
    }
}
