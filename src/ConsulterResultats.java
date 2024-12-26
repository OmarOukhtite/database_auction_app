import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ConsulterResultats extends Options {

    public ConsulterResultats(String email, Scanner scanner, Connection connection) {
        super(email, scanner, connection);
    }

    public int diminuer_le_prix_chaque_minute (int prix_initial,Timestamp dateDebut){
        int diminution_par_minute = (int)(0.05 * prix_initial);
        long tempsPasseMillis = System.currentTimeMillis() - dateDebut.getTime();
        int tempsPasseMinutes = (int)(((float) tempsPasseMillis) / 60000);
        return prix_initial - tempsPasseMinutes * diminution_par_minute;
    }

    public boolean statutVente(int idVente) {
        String queryVenteDureeLimitee = "SELECT DATEFIN FROM VenteDureeLimitee WHERE NUMEROVENTE = ?";
        try (PreparedStatement statementVenteDureeLimitee = connection.prepareStatement(queryVenteDureeLimitee)) {
            statementVenteDureeLimitee.setInt(1, idVente);
    
            try (ResultSet resultSetDuree = statementVenteDureeLimitee.executeQuery()) {
                if (resultSetDuree.next()) {
                    // Verifie si la vente limitee par duree est encore en cours
                    Timestamp dateFin = resultSetDuree.getTimestamp("DATEFIN");
                    long tempsRestantMillis = dateFin.getTime() - System.currentTimeMillis();
    
                    if (tempsRestantMillis > 0) {
                        System.out.println("Vente en cours, date de fin : " + dateFin);
                        return true; // Vente en cours
                    } else {
                        System.out.println("Statut : Vente terminee.");
                        return false; // Vente terminee
                    }
                } else {
                    // Gestion des ventes sans limite de duree
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
                        if (offre != 0){
                            return false;
                        }
                        return true;
                    }
                    else{
                        long dateDerniereOffreMillis = dateDerniereOffre(idVente); // Methode externe
                        long tempsEcouleMillis = System.currentTimeMillis() - dateDerniereOffreMillis;

                        if (dateDerniereOffreMillis == -1) {
                            System.out.println("Statut : Vente en cours.");
                            return true;
                        }
                    
                        if (tempsEcouleMillis > 600000) { // 10 minutes ecoulees
                            System.out.println("Statut : Vente terminee.");
                            return false; // Vente terminee
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
            System.err.println("Erreur lors de la verification du statut de la vente : " + e.getMessage());
            e.printStackTrace();
        }
        return false; // Retourne false par defaut en cas d'erreur
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

    public void attributionVainqueurs(int Id_vente,int NUMEROPRODUIT, Statement statement) throws SQLException{
        // selectionner la meilleure offre
        String requete_sql = "SELECT * FROM utilisateur u "
        + "JOIN offre o ON u.email = o.email "
        + "WHERE o.numerovente =  " + String.valueOf(Id_vente) 
        + " AND u.email IN ( "
        + "  SELECT o1.email FROM offre o1 "
        + "  WHERE o1.numerovente =  " + String.valueOf(Id_vente)
        + "  GROUP BY o1.email "
        + "  HAVING SUM(o1.prixachat) = ("
        + "    SELECT MAX(total) "
        + "    FROM ("
        + "      SELECT SUM(o2.prixachat) AS total "
        + "      FROM offre o2 "
        + "      WHERE o2.numerovente =  " + String.valueOf(Id_vente)
        + "      GROUP BY o2.email"
        + "    ) max_totals"
        + "  )"
        + ")";
        int numero_du_vainqueur = 1;
        String supprimer_les_offres_du_vainqueurs = new String();
        ResultSet resultSet =statement.executeQuery(requete_sql);
        int quantite_vendue = 0;
        String email_du_vainqueur = "";
        int prix_achat = 0;
        while (resultSet.next()) {
            prix_achat += resultSet.getInt("PRIXACHAT");
            quantite_vendue += resultSet.getInt("QUANTITE");
            email_du_vainqueur = resultSet.getString("EMAIL");
        }
        String savoir_si_la_vente_revocable = "SELECT REVOCABLE FROM VENTE WHERE NUMEROVENTE = '"+ Id_vente + "'";
        resultSet =statement.executeQuery(savoir_si_la_vente_revocable);
        String revocable = new String();
        if (resultSet.next()) {
                revocable = resultSet.getString(1);
        }
        int prix_revient = 0;
        resultSet =statement.executeQuery("SELECT PRIXREVIENT FROM PRODUIT WHERE NUMEROPRODUIT = '"+ NUMEROPRODUIT + "'");
        if (resultSet.next()){
            prix_revient = resultSet.getInt(1);
        }
        if ( "Y".equals(revocable) && prix_achat < prix_revient ) {
            System.out.println("La vente est malheureusement annulee car le prix de revient n'a pas ete atteint !");
            supprimer_les_offres_du_vainqueurs = "DELETE FROM OFFRE WHERE EMAIL = '" + email_du_vainqueur +"' AND NUMEROVENTE = '"+ Id_vente + "' " ;
            resultSet = statement.executeQuery(supprimer_les_offres_du_vainqueurs);
        }
        else{
            System.out.println("Le gagnant numero "+ numero_du_vainqueur +" de l'enchere est : ");
            System.out.println(
                " prix achat : " + prix_achat 
                + ", quantite : " + quantite_vendue
                + " email : " + email_du_vainqueur
                );
            supprimer_les_offres_du_vainqueurs = "DELETE FROM OFFRE WHERE EMAIL = '" + email_du_vainqueur +"' AND NUMEROVENTE = '"+ Id_vente + "' " ;
            resultSet = statement.executeQuery(supprimer_les_offres_du_vainqueurs);
            int stock_produit = 0;
            resultSet = statement.executeQuery("SELECT stock FROM PRODUIT WHERE NUMEROPRODUIT = '" + String.valueOf(NUMEROPRODUIT) + "'");
            if (resultSet.next()) {
                stock_produit = resultSet.getInt(1);
            }
            boolean vide = false;
            String check_table_vide ="SELECT COUNT(*) AS total FROM OFFRE WHERE NUMEROVENTE = '"+ Id_vente+"' ";
            resultSet = statement.executeQuery(check_table_vide);
            if (resultSet.next()) {
                int nbre_d_elements = resultSet.getInt(1);
                if (nbre_d_elements == 0){
                    vide = true;
                }
            }
            // selectionner les autres meilleurs offres qui vont permettre a l'enchere de remporter un maximum d'argent tout en ecoulant le stock
            while (quantite_vendue < stock_produit && !vide ){
                    numero_du_vainqueur += 1;
                    resultSet =statement.executeQuery(requete_sql);
                    int quantite_encherie = 0;           
                    while (resultSet.next()) {
                        prix_achat = resultSet.getInt("PRIXACHAT");
                        quantite_encherie += resultSet.getInt("QUANTITE");
                        email_du_vainqueur = resultSet.getString("EMAIL");
                    }
                    if (quantite_vendue + quantite_encherie > stock_produit){
                            System.out.println("quant"+ quantite_vendue + quantite_encherie);
                            supprimer_les_offres_du_vainqueurs = "DELETE FROM OFFRE WHERE EMAIL = '" + email_du_vainqueur +"' AND NUMEROVENTE = '"+ Id_vente + "' " ; ;
                            resultSet = statement.executeQuery(supprimer_les_offres_du_vainqueurs);
                            continue ; 
                    }
                    else{
                            quantite_vendue = quantite_vendue + quantite_encherie;
                            System.out.println("Le gagnant numero "+ numero_du_vainqueur +" de l'enchere est : ");
                            System.out.println(
                                " prix achat : " + prix_achat 
                                + ", quantite : " + quantite_encherie
                                + " email : " + email_du_vainqueur
                            );
                            supprimer_les_offres_du_vainqueurs  = "DELETE FROM OFFRE WHERE EMAIL = '" + email_du_vainqueur +"' AND NUMEROVENTE = '"+ Id_vente + "' " ;
                            resultSet = statement.executeQuery(supprimer_les_offres_du_vainqueurs);
                    }
                    resultSet = statement.executeQuery(check_table_vide);
                    if (resultSet.next()) {
                        int nbre_d_elements = resultSet.getInt(1);
                        if (nbre_d_elements == 0){
                            vide = true;
                        }
                    }
                }

            // mettre a jour le stock du produit dans la relation produit
            int stock_produit_final= stock_produit - quantite_vendue;
            resultSet = statement.executeQuery("UPDATE PRODUIT SET stock = ' "+ String.valueOf(stock_produit_final) +"' WHERE NUMEROPRODUIT = ' " + String.valueOf(NUMEROPRODUIT)+"'");
            if (stock_produit_final <= 0){
                resultSet = statement.executeQuery("DELETE FROM VENTEDUREEILLIMITEE WHERE NUMEROVENTE = '" + Id_vente + "' ");
                resultSet = statement.executeQuery("DELETE FROM VENTEDUREELIMITEE WHERE NUMEROVENTE = '" + Id_vente + "' ");
                resultSet = statement.executeQuery("DELETE FROM VENTE WHERE NUMEROVENTE = '" + Id_vente + "' ");
                resultSet = statement.executeQuery("DELETE FROM CARACTERISTIQUE WHERE NUMEROPRODUIT = '" + NUMEROPRODUIT + "' ");
                resultSet = statement.executeQuery("DELETE FROM PRODUIT WHERE NUMEROPRODUIT = '" + NUMEROPRODUIT + "' ");
                String query = "DELETE FROM SALLE WHERE NUMEROSALLE NOT IN (SELECT NUMEROSALLE FROM VENTE)";
                PreparedStatement statementSalle = connection.prepareStatement(query);
                statementSalle.executeQuery();
            }

        }
    }   

    public void consulterResultats() throws SQLException {
        System.out.println("Les ventes auxquelles vous avez participe :");
        System.out.println("\n -------------------------");
    
        // Preparer une requête pour recuperer les ventes et les produits associes
        String queryVentesParticipes = 
            "SELECT DISTINCT Vente.NUMEROVENTE AS VENTE, Produit.NOMPRODUIT, Produit.NUMEROPRODUIT " +
            "FROM Vente " +
            "JOIN Offre ON Vente.NUMEROVENTE = Offre.NUMEROVENTE " +
            "JOIN Produit ON Vente.NUMEROPRODUIT = Produit.NUMEROPRODUIT " +
            "WHERE Offre.EMAIL = ?";
        try (PreparedStatement statementVentesParticipes = connection.prepareStatement(queryVentesParticipes)) {
            statementVentesParticipes.setString(1, this.email);
            
            try (ResultSet resultSet = statementVentesParticipes.executeQuery()) {
                // Map pour stocker NUMEROVENTE et NUMEROPRODUIT
                Map<Integer, Integer> ventesParticipes = new HashMap<>();
                
                // Remplir la Map avec NUMEROVENTE et NUMEROPRODUIT
                while (resultSet.next()) {
                    int numerovente = resultSet.getInt("VENTE");
                    int numeroproduit = resultSet.getInt("NUMEROPRODUIT");
                    ventesParticipes.put(numerovente, numeroproduit);
                }
            
                // Parcourir les ventes pour recuperer le produit et effectuer les actions necessaires
                for (Map.Entry<Integer, Integer> entry : ventesParticipes.entrySet()) {
                    int venteId = entry.getKey();
                    int numProduit = entry.getValue();
            
                    // Requête pour recuperer le NOMPRODUIT à partir du NUMEROPRODUIT
                    String queryProduit = "SELECT NOMPRODUIT FROM PRODUIT WHERE NUMEROPRODUIT = ?";
                    try (PreparedStatement statementProduit = connection.prepareStatement(queryProduit)) {
                        statementProduit.setInt(1, numProduit);
                        try (ResultSet resultSetProduit = statementProduit.executeQuery()) {
                            String produit = "";
                            if (resultSetProduit.next()) {
                                produit = resultSetProduit.getString("NOMPRODUIT");
                            }
            
                            System.out.println("Vente : " + venteId + " Produit : " + produit);
                            
                            boolean statut = statutVente(venteId);
                            if (!statut) {
                                attributionVainqueurs(venteId, numProduit, connection.createStatement());
                            }
            
                            System.out.println("-------------------------");
                        }
                    } catch (SQLException e) {
                        System.err.println("Erreur lors de la recuperation du produit : " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la consultation des resultats : " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("Tapez entrer pour continuer");
            scanner.nextLine();
            // Revenir au menu
            Tools.clearScreen();
            options();
        }
    }
    
}
