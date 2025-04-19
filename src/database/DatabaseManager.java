package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles database connection and schema creation for the flight punctuality application.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:flights.db";
    private Connection connection;

    /**
     * Creates a new database connection.
     * @throws SQLException if a database access error occurs
     */
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        connection.setAutoCommit(false); // Important for performance during bulk inserts
        System.out.println("Connected to the database.");
    }

    /**
     * Closes the database connection.
     * @throws SQLException if a database access error occurs
     */
    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Database connection closed.");
        }
    }

    /**
     * Creates the database schema, dropping any existing tables.
     * @throws SQLException if a database access error occurs
     */
    public void createSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Drop existing tables if they exist
            stmt.executeUpdate("DROP TABLE IF EXISTS Delay_Reason");
            stmt.executeUpdate("DROP TABLE IF EXISTS Flight");
            stmt.executeUpdate("DROP TABLE IF EXISTS Airline");
            stmt.executeUpdate("DROP TABLE IF EXISTS Airport");

            // Create tables according to the schema

            // Airport table
            stmt.executeUpdate(
                    "CREATE TABLE Airport (" +
                            "iata_code CHAR(3) PRIMARY KEY, " +
                            "name TEXT" +
                            ")"
            );

            // Airline table
            stmt.executeUpdate(
                    "CREATE TABLE Airline (" +
                            "iata_code CHAR(2) PRIMARY KEY, " +
                            "name TEXT" +
                            ")"
            );

            // Flight table
            stmt.executeUpdate(
                    "CREATE TABLE Flight (" +
                            "flight_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "date CHAR(8), " +
                            "airline_code CHAR(2), " +
                            "flight_number INTEGER, " +
                            "flight_origin CHAR(3), " +
                            "flight_destination CHAR(3), " +
                            "scheduled_departure INTEGER, " +
                            "actual_departure INTEGER, " +
                            "scheduled_arrival INTEGER, " +
                            "actual_arrival INTEGER, " +
                            "FOREIGN KEY (airline_code) REFERENCES Airline(iata_code), " +
                            "FOREIGN KEY (flight_origin) REFERENCES Airport(iata_code), " +
                            "FOREIGN KEY (flight_destination) REFERENCES Airport(iata_code)" +
                            ")"
            );

            // Delay_Reason table
            stmt.executeUpdate(
                    "CREATE TABLE Delay_Reason (" +
                            "delay_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "flight_id INTEGER, " +
                            "reason TEXT, " +
                            "delay_length INTEGER, " +
                            "FOREIGN KEY (flight_id) REFERENCES Flight(flight_id)" +
                            ")"
            );

            connection.commit();
            System.out.println("Database schema created.");
        }
    }

    /**
     * Creates indices to improve query performance.
     * @throws SQLException if a database access error occurs
     */
    public void createIndices() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create indices on columns that will be frequently queried
            stmt.executeUpdate("CREATE INDEX idx_flight_date ON Flight(date)");
            stmt.executeUpdate("CREATE INDEX idx_flight_origin ON Flight(flight_origin)");
            stmt.executeUpdate("CREATE INDEX idx_flight_dest ON Flight(flight_destination)");
            stmt.executeUpdate("CREATE INDEX idx_flight_airline ON Flight(airline_code)");
            stmt.executeUpdate("CREATE INDEX idx_flight_number ON Flight(flight_number)");
            stmt.executeUpdate("CREATE INDEX idx_delay_reason_flight ON Delay_Reason(flight_id)");
            stmt.executeUpdate("CREATE INDEX idx_delay_reason ON Delay_Reason(reason)");

            connection.commit();
            System.out.println("Database indices created.");
        }
    }

    /**
     * Gets the current database connection.
     * @return the database connection
     */
    public Connection getConnection() {
        return connection;
    }
}