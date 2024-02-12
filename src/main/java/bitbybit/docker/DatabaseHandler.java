package bitbybit.docker;

import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DatabaseHandler {
    private static String dbName = "container_metrics.db";
    public static Queue<MonitorThread.ContainerMetrics> metrics = new LinkedList<>() {
    };

    /* Path to the embedded database file in resources*/
    private static String dbPath = "src/main/resources/" + dbName;

    /* SQLite database URL for the embedded database*/
    private static String url = "jdbc:sqlite:" + dbPath;

    public static void form_connection() {

        try {
            Class.forName("org.sqlite.JDBC");
                createDatabaseFile(dbPath);

            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                try (Statement statement = connection.createStatement()) {
                    createTables(statement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IOException("Error creating database file.", e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void createDatabaseFile(String filePath) throws IOException {
        File databaseFile = new File(filePath);

        // Check if the file already exists or create a new file
        if (!databaseFile.exists()) {
            try {
                // Create the directory structure if it doesn't exist
                databaseFile.getParentFile().mkdirs();

                // Create a new empty file
                if (databaseFile.createNewFile()) {
                    System.out.println("Database file created: " + filePath);
                } else {
                    throw new IOException("Failed to create database file.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("Error creating database file.", e);
            }
        }
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
                "NetworkRx REAL, " +
                "NetworkTx REAL, " +
                "FOREIGN KEY (container_id) REFERENCES containers(container_id))";
        statement.executeUpdate(createMetricsTableQuery);
    }

    /*add container metrics to db*/
    public static void add_metrics(MonitorThread.ContainerMetrics c) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /*add a container to the database*/

    public static void add_container(String id, String name, String image) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO containers (container_id, container_name, image_name) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, image);
            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            }


    }
    /*add metric to mon thread list*/

    public static void addm(MonitorThread.ContainerMetrics c) {
        metrics.add(c);
    }

    /*Get queries*/

    public static String  getmeasurement(String d) {
        try (Connection connection = DriverManager.getConnection(url)) {
            String query = "SELECT metric_id,container_name, timestamp,CPUusage, memoryusage, NetworkRx, NetworkTx FROM metrics AS M, containers AS C WHERE DATE(timestamp)=DATE(?) AND C.container_id = M.container_id";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date parsedDate = dateFormat.parse(d);
                java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                preparedStatement.setString(1, sqlDate.toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    StringBuilder resultStringBuilder = new StringBuilder();
                    try {

                        resultStringBuilder.append("Metric ID   Container Name   Timestamp   CPU Usage   Memory Usage   Network Rx   Network Tx\n");

                        // Iterate over the result set and append values to the StringBuilder
                        while (resultSet.next()) {
                            int metricId = resultSet.getInt("metric_id");
                            String containerId = resultSet.getString("container_name");
                            Date timestamp = resultSet.getDate("timestamp");
                            double cpuUsage = resultSet.getDouble("CPUusage");
                            double memoryUsage = resultSet.getDouble("memoryUsage");
                            double networkRx = resultSet.getDouble("NetworkRx");
                            double networkTx = resultSet.getDouble("NetworkTx");

                            // Format the timestamp
                            SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
                            String formattedTimestamp = dateFormat2.format(timestamp);

                            // Append values to the StringBuilder
                            resultStringBuilder.append(metricId).append("              ").append(containerId).append("                 ")
                                    .append(formattedTimestamp).append("        ").append(cpuUsage).append("              ")
                                    .append(memoryUsage).append("                     ").append(networkRx).append("     ").append(networkTx)
                                    .append("\n");

                        }
                        return resultStringBuilder.toString();
                    } catch (SQLException e){
                        e.printStackTrace();
                    }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String  getidmeasurement(String id) {
        try (Connection connection = DriverManager.getConnection(url)) {
            String query = "SELECT metric_id,container_name, timestamp,CPUusage, memoryusage, NetworkRx, NetworkTx FROM metrics AS M, containers AS C WHERE C.container_id = ? AND C.container_id = M.container_id";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setString(1, id);


                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    StringBuilder resultStringBuilder = new StringBuilder();
                    try {

                        resultStringBuilder.append("Metric ID   Container Name   Timestamp   CPU Usage   Memory Usage   Network Rx   Network Tx\n");

                        // Iterate over the result set and append values to the StringBuilder
                        while (resultSet.next()) {
                            int metricId = resultSet.getInt("metric_id");
                            String containerId = resultSet.getString("container_name");
                            Date timestamp = resultSet.getDate("timestamp");
                            double cpuUsage = resultSet.getDouble("CPUusage");
                            double memoryUsage = resultSet.getDouble("memoryUsage");
                            double networkRx = resultSet.getDouble("NetworkRx");
                            double networkTx = resultSet.getDouble("NetworkTx");

                            // Format the timestamp
                            SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
                            String formattedTimestamp = dateFormat2.format(timestamp);

                            // Append values to the StringBuilder
                            resultStringBuilder.append(metricId).append("           ").append(containerId).append("            ")
                                    .append(formattedTimestamp).append("       ").append(cpuUsage).append("          ")
                                    .append(memoryUsage).append("          ").append(networkRx).append("              ").append(networkTx)
                                    .append("\n");

                        }
                        return resultStringBuilder.toString();
                    } catch (SQLException e){
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String  getdateidmeasurement(String id,String date) {
        try (Connection connection = DriverManager.getConnection(url)) {
            String query = "SELECT metric_id,container_name, timestamp,CPUusage, memoryusage, NetworkRx, NetworkTx FROM metrics AS M, containers AS C WHERE DATE(timestamp)=DATE(?) AND C.container_id = ? AND C.container_id = M.container_id";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {


                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date parsedDate = dateFormat.parse(date);
                java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                preparedStatement.setString(1, sqlDate.toString());
                preparedStatement.setString(2, id);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    StringBuilder resultStringBuilder = new StringBuilder();
                    try {

                        resultStringBuilder.append("Metric ID   Container Name   Timestamp   CPU Usage   Memory Usage   Network Rx   Network Tx\n");

                        // Iterate over the result set and append values to the StringBuilder
                        while (resultSet.next()) {
                            int metricId = resultSet.getInt("metric_id");
                            String containerId = resultSet.getString("container_name");
                            Date timestamp = resultSet.getDate("timestamp");
                            double cpuUsage = resultSet.getDouble("CPUusage");
                            double memoryUsage = resultSet.getDouble("memoryUsage");
                            double networkRx = resultSet.getDouble("NetworkRx");
                            double networkTx = resultSet.getDouble("NetworkTx");

                            // Format the timestamp
                            SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
                            String formattedTimestamp = dateFormat2.format(timestamp);

                            // Append values to the StringBuilder
                            resultStringBuilder.append(metricId+"\t").append(containerId).append("    \t")
                                    .append(formattedTimestamp).append("    \t").append(cpuUsage).append("    \t")
                                    .append(memoryUsage).append("    \t").append(networkRx).append("    \t").append(networkTx)
                                    .append("\n");


                        }
                        return resultStringBuilder.toString();
                    } catch (SQLException e){
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ParseException e){
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    }


