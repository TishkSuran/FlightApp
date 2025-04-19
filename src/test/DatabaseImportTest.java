package test;

import database.DatabaseManager;
import database.CsvImporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test class for the Database Import functionality.
 */
public class DatabaseImportTest {

    private static final String TEST_DB_URL = "jdbc:sqlite:test_flights.db";
    private static final String TEST_CSV_FILE = "test_flights.csv";

    /**
     * Main method to run tests.
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("Running Database Import Tests...");

        try {
            // Create a test CSV file
            createTestCsvFile();

            // Create database manager for testing
            DatabaseManager dbManager = new TestDatabaseManager();

            // Connect to database
            dbManager.connect();

            // Create schema
            dbManager.createSchema();

            // Import CSV data
            CsvImporter importer = new CsvImporter(dbManager.getConnection());
            importer.importCsv(TEST_CSV_FILE);

            // Create indices
            dbManager.createIndices();

            // Verify the imported data
            verifyImportedData(dbManager.getConnection());

            // Cleanup
            dbManager.disconnect();
            new File(TEST_CSV_FILE).delete(); // Delete test CSV file
            new File("test_flights.db").delete(); // Delete test database

            System.out.println("All tests passed!");

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a test CSV file with a few rows of data.
     * @throws IOException if an I/O error occurs
     */
    private static void createTestCsvFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_CSV_FILE))) {
            // Write header
            writer.write("FL_DATE,AIRLINE,AIRLINE_CODE,FL_NUMBER,ORIGIN,ORIGIN_CITY,DEST,DEST_CITY," +
                    "CRS_DEP_TIME,DEP_TIME,CRS_ARR_TIME,ARR_TIME,CANCELLED," +
                    "DELAY_DUE_CARRIER,DELAY_DUE_WEATHER,DELAY_DUE_NAS,DELAY_DUE_SECURITY," +
                    "DELAY_DUE_LATE_AIRCRAFT\n");

            // Write a few data rows
            writer.write("20210101,Delta Air Lines,DL,1234,ATL,Atlanta,LAX,Los Angeles,900,910,1200,1210,0,10,0,0,0,0\n");
            writer.write("20210102,American Airlines,AA,4321,JFK,New York,SFO,San Francisco,1000,1115,1400,1530,0,45,30,0,0,0\n");
            writer.write("20210103,United Airlines,UA,5678,ORD,Chicago,DEN,Denver,1300,1300,1500,1455,0,0,0,0,0,0\n");
        }
        System.out.println("Created test CSV file: " + TEST_CSV_FILE);
    }

    /**
     * Verifies that data was correctly imported into the database.
     * @param connection the database connection
     * @throws SQLException if a database access error occurs
     */
    private static void verifyImportedData(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Check airlines
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Airline")) {
                assert rs.next() && rs.getInt(1) == 3 : "Expected 3 airlines";
                System.out.println("✓ Airlines count verified");
            }

            // Check airports
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Airport")) {
                assert rs.next() && rs.getInt(1) == 6 : "Expected 6 airports";
                System.out.println("✓ Airports count verified");
            }

            // Check flights
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Flight")) {
                assert rs.next() && rs.getInt(1) == 3 : "Expected 3 flights";
                System.out.println("✓ Flights count verified");
            }

            // Check delay reasons
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Delay_Reason")) {
                assert rs.next() && rs.getInt(1) == 3 : "Expected 3 delay reasons";
                System.out.println("✓ Delay reasons count verified");
            }

            // Check specific flight details
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT f.date, a.name, f.flight_number " +
                            "FROM Flight f JOIN Airline a ON f.airline_code = a.iata_code " +
                            "WHERE f.flight_number = 1234")) {

                assert rs.next() : "Expected to find flight 1234";
                assert rs.getString(1).equals("20210101") : "Expected flight date 20210101, got " + rs.getString(1);
                assert rs.getString(2).equals("Delta Air Lines") : "Expected Delta Air Lines, got " + rs.getString(2);
                assert rs.getInt(3) == 1234 : "Expected flight number 1234, got " + rs.getInt(3);
                System.out.println("✓ Flight details verified");
            }
        }
    }

    /**
     * Test-specific database manager that uses a test-specific database URL.
     */
    private static class TestDatabaseManager extends DatabaseManager {
        @Override
        public void connect() throws SQLException {
            Connection connection = java.sql.DriverManager.getConnection(TEST_DB_URL);
            connection.setAutoCommit(false);
            java.lang.reflect.Field field;
            try {
                field = DatabaseManager.class.getDeclaredField("connection");
                field.setAccessible(true);
                field.set(this, connection);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set test connection", e);
            }
        }
    }
}