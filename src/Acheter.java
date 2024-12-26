import java.sql.*;
import java.util.Scanner;

public class Acheter extends Options {
    public String email; 

    public Acheter(String email, Scanner scanner, Connection connection) {
        super(email, scanner, connection);
        this.email = email;
    }
    public int diminuer_le_prix_chaque_minute (int prix_initial,Timestamp dateDebut){
        int diminution_par_minute = (int)(0.05 * prix_initial);
        long tempsPasseMillis = System.currentTimeMillis() - dateDebut.getTime();
        int tempsPasseMinutes = (int)(((float) tempsPasseMillis) / 60000);
        return prix_initial - tempsPasseMinutes * diminution_par_minute;
    }

    public boolean plusieursOffres(int idVente) {
        String query = "SELECT PLUSIEURSOFFRESPARUTILISATEUR FROM Vente WHERE NUMEROVENTE = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, idVente);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next() && "Y".equals(resultSet.getString("PLUSIEURSOFFRESPARUTILISATEUR"))) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean statutVente(int idVente) {
        String queryVenteDureeLimitee = "SELECT DATEFIN FROM VenteDureeLimitee WHERE NUMEROVENTE = ?";
        try (PreparedStatement statementVenteDureeLimitee = connection.prepareStatement(queryVenteDureeLimitee)) {
            statementVenteDureeLimitee.setInt(1, idVente);
    
            try (ResultSet resultSetDuree = statementVenteDureeLimitee.executeQuery()) {
                if (resultSetDuree.next()) {
                    // Vérifie si la vente limitée par durée est encore en cours
                    Timestamp dateFin = resultSetDuree.getTimestamp("DATEFIN");
                    long tempsRestantMillis = dateFin.getTime() - System.currentTimeMillis();
    
                    if (tempsRestantMillis > 0) {
                        System.out.println("Vente en cours, date de fin : " + dateFin);
                        return true; // Vente en cours
                    } else {
                        System.out.println("Statut : Vente terminée.");
                        return false; // Vente terminée
                    }
                } else {
                    // Gestion des ventes sans limite de durée
                    // voir si la vente est montante ou descendante
                    String query = "SELECT MONTANTE FROM Vente WHERE NUMEROVENTE = '"+ idVente + "' ";
                    PreparedStatement statement = null;
                    statement = connection.prepareStatement(query);
                    ResultSet resultSet = statement.executeQuery();
                    String montante = new String();
                    if (resultSet.next()){
                        montante = resultSet.getString(1);
                        }
                    if("N".equals(montante)) {
                        query = "SELECT PRIXDEPART, DATEDEBUT FROM Vente WHERE NUMEROVENTE = '"+ idVente + "' ";
                        statement = connection.prepareStatement(query);
                        resultSet = statement.executeQuery();
                        Timestamp date_debut = null;
                        int prix_initial = 0;
                        while (resultSet.next()){
                                date_debut = resultSet.getTimestamp("DATEDEBUT");
                                prix_initial = resultSet.getInt("PRIXDEPART"); 
                        }
                        int nouveau_prix = diminuer_le_prix_chaque_minute (prix_initial,date_debut);
                        if ( nouveau_prix <= 0){
                            System.out.println("Dommage, cette vente descendante a ete terminee sans aucune offre !");
                            return false;
                        }
                        query = "SELECT COUNT(*) AS nombre_offres FROM Offre WHERE NUMEROVENTE = '" + idVente + "'";
                        statement = connection.prepareStatement(query);
                        resultSet = statement.executeQuery();
                        int offre = 0;
                        if (resultSet.next()){
                            offre = resultSet.getInt(1);
                        }
                        if (offre == 1){
                            return false;
                        }
                        return true;
                    }
                    else{
                        long dateDerniereOffreMillis = dateDerniereOffre(idVente); // Méthode externe
                        long tempsEcouleMillis = System.currentTimeMillis() - dateDerniereOffreMillis;

                        if (dateDerniereOffreMillis == -1) {
                            System.out.println("Statut : Vente en cours.");
                            return true;
                        }
                    
                        if (tempsEcouleMillis > 600000) { // 10 minutes écoulées
                            System.out.println("Statut : Vente terminée.");
                            return false; // Vente terminée
                        } else {
                            // Temps restant avant la fin de la vente
                            long tempsRestantMinutes = (600000 - tempsEcouleMillis) / 60000;
                            long tempsRestantSecondes = ((600000 - tempsEcouleMillis) / 1000) % 60;
                            System.out.println("Vente en cours, temps restant avant fin : " 
                                + tempsRestantMinutes + " minute(s) " + tempsRestantSecondes + " seconde(s).");
                            return true; // Vente en cours
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // Gestion des exceptions SQL
            System.err.println("Erreur lors de la vérification du statut de la vente : " + e.getMessage());
            e.printStackTrace();
        }
        return false; // Retourne false par défaut en cas d'erreur
    }

    public long dateDerniereOffre(int venteId) throws SQLException {
        PreparedStatement offreStatement = connection.prepareStatement(
            "SELECT * FROM Offre WHERE NUMEROVENTE = ? AND DATEOFFRE = (" +
            "    SELECT MAX(DATEOFFRE) FROM Offre WHERE NUMEROVENTE = ?" +
            ")"
        );
        offreStatement.setInt(1, venteId);
        offreStatement.setInt(2, venteId);
    
        ResultSet offreResultSet = offreStatement.executeQuery();
    
        if (offreResultSet.next()) {
            Timestamp dateDerniereOffre = offreResultSet.getTimestamp("DATEOFFRE");
            return dateDerniereOffre.getTime();
        } else {
            return -1;
        }
    }

    public void acheter() throws SQLException {

        ResultSet resultSet = null;
        PreparedStatement statement = null;
    
        try {
            System.out.println("Salles disponibles : \n");
    
            // Récupérer et afficher les salles disponibles
            String querySalles = "SELECT * FROM Salle";
            statement = connection.prepareStatement(querySalles);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                System.out.println("Salle " + resultSet.getString("NUMEROSALLE") + " : " + resultSet.getString("NOMCATEGORIE"));
            }
    
            int numSalle = -1;
            boolean salleValide = false;
    
            while (!salleValide) {
                System.out.println("Choisissez une salle ? (ou tapez 'annuler')");
                String inputSalle = scanner.nextLine();
    
                if (inputSalle.equalsIgnoreCase("annuler")) {
                    Tools.clearScreen();
                    options();
                    return;
                }
    
                try {
                    numSalle = Integer.parseInt(inputSalle);
                } catch (NumberFormatException e) {
                    System.out.println("Veuillez entrer un numéro valide.");
                    continue;
                }
    
                String querySalleValide = "SELECT * FROM Salle WHERE NUMEROSALLE = ?";
                try (PreparedStatement statementSalleValide = connection.prepareStatement(querySalleValide)) {
                    statementSalleValide.setInt(1, numSalle);
                    try (ResultSet resultSetSalleValide = statementSalleValide.executeQuery()) {
                        if (resultSetSalleValide.next()) {
                            salleValide = true;
                        } else {
                            System.out.println("Salle invalide, réessayez.");
                        }
                    }
                }
            }
            Tools.clearScreen();
            System.out.println("Produits disponibles :");
            String queryProduits =
                "SELECT Vente.NUMEROVENTE, Produit.NOMPRODUIT, Produit.STOCK, Vente.PRIXDEPART, " +
                "Vente.REVOCABLE, Vente.MONTANTE, Vente.PLUSIEURSOFFRESPARUTILISATEUR " +
                "FROM Vente " +
                "JOIN Produit ON Vente.NUMEROPRODUIT = Produit.NUMEROPRODUIT " +
                "WHERE Vente.NUMEROSALLE = ?";
            try (PreparedStatement statementProduits = connection.prepareStatement(queryProduits)) {
                statementProduits.setInt(1, numSalle);
                try (ResultSet resultSetProduits = statementProduits.executeQuery()) {
                    while (resultSetProduits.next()) {
                        int idVente = resultSetProduits.getInt("NUMEROVENTE");
                        System.out.println("\n-------------------------\n"
                            + "Vente : " + idVente + "\n"
                            + "Salle : " + numSalle + "\n"
                            + "Produit : " + resultSetProduits.getString("NOMPRODUIT") + "\n"
                            + "Stock : " + resultSetProduits.getInt("STOCK") + "\n"
                            + "Prix de départ : " + resultSetProduits.getInt("PRIXDEPART") + "\n"
                            + "Révocable : " + resultSetProduits.getString("REVOCABLE") + "\n"
                            + "Montante : " + resultSetProduits.getString("MONTANTE") + "\n"
                            + "Plusieurs offres par utilisateur : " + resultSetProduits.getString("PLUSIEURSOFFRESPARUTILISATEUR") + "\n"
                        );

                        statutVente(idVente);
                            
                        System.out.println("-------------------------");
                    }
                }
            }
    
            int numVente = -1;
            int stockProduit = 0;
            boolean venteValide = false;

            while (!venteValide) {
                System.out.println("Choisissez une vente a laquelle vous voulez participer : (donner le numéro ou tapez 'annuler')");
                String inputVente = scanner.nextLine();

                if (inputVente.equalsIgnoreCase("annuler")) {
                    Tools.clearScreen();
                    options();
                    return;
                }

                try {
                    numVente = Integer.parseInt(inputVente); // Conversion du numéro de vente
                } catch (NumberFormatException e) {
                    System.out.println("Veuillez entrer un numéro valide.");
                    continue; // Retourne au début de la boucle
                }

                String queryVente = 
                    "SELECT Produit.STOCK " +
                    "FROM Vente " +
                    "JOIN Produit ON Produit.NUMEROPRODUIT = Vente.NUMEROPRODUIT " +
                    "WHERE Vente.NUMEROVENTE = ?" + 
                    "AND NUMEROSALLE = ?";
                
                try (PreparedStatement statementVente = connection.prepareStatement(queryVente)) {
                    statementVente.setInt(1, numVente); // Remplace le paramètre dans la requête
                    statementVente.setInt(2, numSalle);
                    try (ResultSet resultSetVente = statementVente.executeQuery()) {
                        if (resultSetVente.next() && statutVente(numVente)) {
                            // Vente valide et statut actif
                            stockProduit = resultSetVente.getInt("STOCK"); // Récupère le stock
                            venteValide = true; // Fin de la boucle
                        } else {
                            // Vente invalide ou terminée
                            System.out.println("Vente invalide ou terminée, réessayez.");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            Statement statementPlusieursOffres = connection.createStatement();
            // voir si lutilisateur a deja fait une offre sur cette vente
            ResultSet resultSetPlusieursOffres = statementPlusieursOffres.executeQuery(
                "SELECT * FROM Offre WHERE EMAIL = '" + this.email + "' AND NUMEROVENTE = " + numVente
            );
            boolean plusieursOffres = plusieursOffres(numVente);
            if (resultSetPlusieursOffres.next() && !plusieursOffres) {
                System.out.println("Vous avez déjà fait une offre sur cette vente.");
                options();
                return;
            }



            // Afficher la meilleure offre pour le produit choisi
            String requeteSQL = 
                "SELECT * FROM utilisateur u " +
                "JOIN offre o ON u.email = o.email " +
                "WHERE o.numerovente = ? " +
                "AND u.email IN (" +
                "  SELECT o1.email FROM offre o1 " +
                "  WHERE o1.numerovente = ? " +
                "  GROUP BY o1.email " +
                "  HAVING SUM(o1.prixachat) = (" +
                "    SELECT MAX(total) FROM (" +
                "      SELECT SUM(o2.prixachat) AS total " +
                "      FROM offre o2 " +
                "      WHERE o2.numerovente = ? " +
                "      GROUP BY o2.email" +
                "    ) max_totals" +
                "  )" +
                ")";
            int meilleurPrix = 0;
            try (PreparedStatement statementMeilleureOffre = connection.prepareStatement(requeteSQL)) {
                statementMeilleureOffre.setInt(1, numVente);
                statementMeilleureOffre.setInt(2, numVente);
                statementMeilleureOffre.setInt(3, numVente);

                try (ResultSet resultSetMeilleureOffre = statementMeilleureOffre.executeQuery()) {
                    while (resultSetMeilleureOffre.next()) {
                        meilleurPrix += resultSetMeilleureOffre.getInt("PRIXACHAT");
                    }
                }
            }
            Tools.clearScreen();
            if (meilleurPrix == 0) {
                System.out.println("Pas d'offre pour le moment");
            } else {
                System.out.println("Meilleur prix : " + meilleurPrix);
            }

            // Faire une offre
            boolean validInput = false;

            while (!validInput) {
                System.out.println("Faire une offre ? (oui/non)");
                String inputOffre = scanner.nextLine();

                if (inputOffre.equalsIgnoreCase("non")) {
                    Tools.clearScreen();
                    options();
                    validInput = true;
                } else if (inputOffre.equalsIgnoreCase("oui")) {
                    // voir si la vente est montante ou descendante
                    String query = "SELECT MONTANTE FROM Vente WHERE NUMEROVENTE = '"+ numVente + "' ";
                    resultSet = statement.executeQuery(query);
                    String montante = new String();
                    if (resultSet.next()){
                        montante = resultSet.getString(1);
                        }
                    if( "N".equals(montante)){
                        query = "SELECT PRIXDEPART, DATEDEBUT FROM Vente WHERE NUMEROVENTE = '"+ numVente + "' ";
                        resultSet = statement.executeQuery(query);
                        Timestamp date_debut = null;
                        int prix_initial = 0;
                        while (resultSet.next()){
                                date_debut = resultSet.getTimestamp("DATEDEBUT");
                                prix_initial = resultSet.getInt("PRIXDEPART"); 
                        }
                        int nouveau_prix = diminuer_le_prix_chaque_minute (prix_initial,date_debut);
                        System.out.println("Cette vente est descendante, le prix de depart actuelle est : " + nouveau_prix);
                        System.out.println("Prix achat ?");
                        int prixAchat = Integer.parseInt(scanner.nextLine());
                        System.out.println("Quantité ?");
                        int quantite = Integer.parseInt(scanner.nextLine());
                        // Vérifier si l'offre est valide
                        if (prixAchat > nouveau_prix && quantite  <= stockProduit && statutVente(numVente)) {
                            validInput = true;
                            try (PreparedStatement statementInsertOffre = connection.prepareStatement(
                                    "INSERT INTO Offre (NUMEROOFFRE, PRIXACHAT, DATEOFFRE, QUANTITE, EMAIL, NUMEROVENTE) " +
                                    "VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?)")) {
                                
                                statementInsertOffre.setInt(1, 10009);
                                statementInsertOffre.setInt(2, prixAchat);
                                statementInsertOffre.setInt(3, quantite);
                                statementInsertOffre.setString(4, this.email);
                                statementInsertOffre.setInt(5, numVente);
                                if (statutVente(numVente)){
                                    statementInsertOffre.executeUpdate();
                                    System.out.println("Offre ajoutée !");
                                    System.out.println("Tapez entrer pour continuer" );
                                }
                                else{
                                    System.out.println("Dommage ! Tu l'as rate de peu !");
                                }
                                scanner.nextLine();
                                Tools.clearScreen();
                            }
                        } else {
                            if (!statutVente(numVente)){
                                System.out.println("Vente terminee !");
                            }
                            else {
                            System.out.println("Offre invalide, réessayez.");
                            }
                        }
                    }
                    // cas des ventes montantes
                    else{
                        System.out.println("Prix achat ?");
                        int prixAchat = Integer.parseInt(scanner.nextLine());
                        System.out.println("Quantité ?");
                        int quantite = Integer.parseInt(scanner.nextLine());
                        // Récupérer le numéro pour l'offre
                        int numOffre = 0;
                        try (PreparedStatement statementNumOffre = connection.prepareStatement("SELECT MAX(NUMEROOFFRE) FROM Offre");
                            ResultSet resultSetNumOffre = statementNumOffre.executeQuery()) {
                            if (resultSetNumOffre.next()) {
                                numOffre = resultSetNumOffre.getInt(1) + 1;
                            } else {
                                numOffre = 1;
                            }
                        }

                        // Récupérer la somme des quantités enchéries par l'utilisateur
                        int sommeQuantite = 0;
                        try (PreparedStatement statementSommeQuantite = connection.prepareStatement(
                                "SELECT SUM(QUANTITE) FROM Offre WHERE EMAIL = ?")) {
                            statementSommeQuantite.setString(1, this.email);
                            try (ResultSet resultSetSommeQuantite = statementSommeQuantite.executeQuery()) {
                                if (resultSetSommeQuantite.next()) {
                                    sommeQuantite = resultSetSommeQuantite.getInt(1);
                                }
                            }
                        }

                        // Récupérer la somme des prix d'achat par l'utilisateur
                        int sommePrix = 0;
                        try (PreparedStatement statementSommePrix = connection.prepareStatement(
                                "SELECT SUM(PRIXACHAT) FROM Offre WHERE EMAIL = ?")) {
                            statementSommePrix.setString(1, this.email);
                            try (ResultSet resultSetSommePrix = statementSommePrix.executeQuery()) {
                                if (resultSetSommePrix.next()) {
                                    sommePrix = resultSetSommePrix.getInt(1);
                                }
                            }
                        }

                        // Vérifier si l'offre est valide
                        if (prixAchat + sommePrix > meilleurPrix && quantite + sommeQuantite <= stockProduit  && statutVente(numVente)) {
                            validInput = true;
                            try (PreparedStatement statementInsertOffre = connection.prepareStatement(
                                    "INSERT INTO Offre (NUMEROOFFRE, PRIXACHAT, DATEOFFRE, QUANTITE, EMAIL, NUMEROVENTE) " +
                                    "VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?)")) {
                                statementInsertOffre.setInt(1, numOffre);
                                statementInsertOffre.setInt(2, prixAchat);
                                statementInsertOffre.setInt(3, quantite);
                                statementInsertOffre.setString(4, this.email);
                                statementInsertOffre.setInt(5, numVente);
                                statementInsertOffre.executeUpdate();
                                System.out.println("Offre ajoutée !");
                                System.out.println("Tapez entrer pour continuer" );
                                scanner.nextLine();
                                Tools.clearScreen();
                            }
                        } else {
                            System.out.println("Offre invalide, réessayez.");
                        }
                    }

                } else {
                    System.out.println("Choix invalide, réessayez.");
                }
            }
    
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
        }
        options();
    }
    
    
}
