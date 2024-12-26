import java.sql.*;

public class AjusterPrixRunnable implements Runnable {
    private Connection connection;
    private int numeroVente;
    private boolean isRunning;

    public AjusterPrixRunnable(Connection connection, int numeroVente) {
        this.connection = connection;
        this.numeroVente = numeroVente;
        this.isRunning = true;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                int nouveauPrix = ajusterPrix();
                updatePrixVente(nouveauPrix);

                // Attendre un certain delai avant de reajuster (par exemple, 1 minute)
                Thread.sleep(60000); // Attendre 60 secondes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private int ajusterPrix() {
        // Recuperer l'ancien prix de depart
        int prixActuel = 0;
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT PrixDepart FROM Vente WHERE NumeroVente = ?")) {
            stmt.setInt(1, numeroVente);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                prixActuel = rs.getInt("PrixDepart");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return (int) (prixActuel - 5);
    }

    private void updatePrixVente(int nouveauPrix) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE Vente SET PrixDepart = ? WHERE NumeroVente = ?")) {
            stmt.setInt(1, nouveauPrix);
            stmt.setInt(2, numeroVente);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Arrêter le Runnable si necessaire
    public void stop() {
        isRunning = false;
    }
}
