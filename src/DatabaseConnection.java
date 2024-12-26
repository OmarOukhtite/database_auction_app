import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import oracle.jdbc.driver.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
    private static final String USER = "chlihim";
    private static final String PASSWORD = "chlihim";

    public static Connection getConnection() throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void closeConnection(Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
