package exam_easv_belman.DAL;

//Java Imports
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

public class DBConnector {

    private static final String PROPERTY_FILE = "config/database.settings";
    private static SQLServerDataSource dataSource;

    /**
     * Constructs a DBConnector and initializes the database connection pool.
     *
     * This constructor reads database connection details (server, database name,
     * username, and password) from config/database.settings. It then configures a
     * SQLServerDataSource with these properties, setting the port to 1433
     * and enabling server certificate trust.
     *
     * @throws IOException If an error occurs while reading the properties file.
     */
    public DBConnector() throws IOException {


        Properties dbProps = new Properties();
        dbProps.load(new FileInputStream(PROPERTY_FILE));

        dataSource = new SQLServerDataSource();
        dataSource.setServerName(dbProps.getProperty("Server"));
        dataSource.setDatabaseName(dbProps.getProperty("Database"));
        dataSource.setUser(dbProps.getProperty("User"));
        dataSource.setPassword(dbProps.getProperty("Password"));
        dataSource.setPortNumber(1433);
        dataSource.setTrustServerCertificate(true);

    }

    public Connection getConnection() throws SQLServerException {
        return dataSource.getConnection();
    }
}