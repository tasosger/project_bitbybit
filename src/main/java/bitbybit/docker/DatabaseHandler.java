package bitbybit.docker;

import com.github.dockerjava.api.exception.NotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class DatabaseHandler {
    private static String dbName = "container_metrics.db";

    // Path to the embedded database file in resources
    private static String dbPath = "src/main/resources/" + dbName;

    // SQLite database URL for the embedded database
    private static String url = "jdbc:sqlite:" + dbPath;
    public static void form_connection()  {

    try {
        Class.forName("org.sqlite.JDBC");
        if (!Files.exists(Path.of(dbPath))) {
            createDatabaseFile(dbPath);
        }
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            try (Statement statement = connection.createStatement()) {
                createTables(statement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOException("Error creating database file.", e);
        }
    }catch ( IOException e) {
        e.printStackTrace();
    }catch (ClassNotFoundException e){
        e.printStackTrace();
    }
    }
    private static void createDatabaseFile(String filePath) throws IOException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filePath)) {
            try (Statement statement = connection.createStatement()) {
               //createTables(statement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOException("Error creating database file.", e);
        }
    }
    private static void createTables(Statement statement) throws SQLException {
        String createContainersTableQuery = "CREATE TABLE IF NOT EXISTS containers ( " +
                "container_id TEXT PRIMARY KEY, " +
                "container_name TEXT UNIQUE, " +
                "image_name TEXT NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)";
        statement.executeUpdate(createContainersTableQuery);

        String createMetricsTableQuery = "CREATE TABLE IF NOT EXISTS metrics (" +
                "metric_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "container_id TEXT, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "CPUusage REAL, " +
                "memoryUsage REAL, " +
                "NetoworkRx REAL, " +
                "NetworkTx REAL, " +
                "FOREIGN KEY (container_id) REFERENCES containers(container_id))";
        statement.executeUpdate(createMetricsTableQuery);
    }
    public static void add_metrics(MonitorThread.ContainerMetrics c){
        try{
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO metrics (container_id, CPUusage, memoryUsage, NetworkRx, NetworkTx) VALUES (?, ?, ?, ?, ?)")) {
            preparedStatement.setString(1, c.getContainerID());
            preparedStatement.setDouble(2, c.getCPUusage());
            preparedStatement.setDouble(3, c.getMeasurement());
            preparedStatement.setDouble(4, c.getNetworkRx());
            preparedStatement.setDouble(5, c.getNetworkTx());
            preparedStatement.executeUpdate();

        }catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void add_container(String id, String name, String image){
        try{
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO containers (container_id, container_name, image_name) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, image);
            preparedStatement.executeUpdate();


        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
