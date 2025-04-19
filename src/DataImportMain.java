import database.CsvImporter;
import database.DatabaseManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Main class for the Flight Punctuality Data Import Program.
 * This program imports flight data from a CSV file into an SQLite database.
 */
public class DataImportMain {

    /**
     * Main entry point for the application.
     * @param args command line arguments (optional path to CSV file)
     */
    public static void main(String[] args) {
        System.out.println("Flight Punctuality Data Import Program");
        System.out.println("-------------------------------------");

        // Process command line arguments
        String csvFilePath = args.length > 0 ? args[0] : "src/flights.csv";

        // Check if file exists
        File csvFile = new File(csvFilePath);
        if (!csvFile.exists() || !csvFile.isFile()) {
            System.err.println("Error: CSV file not found: " + csvFilePath);
            System.exit(1);
        }

        System.out.println("Using CSV file: " + csvFilePath);

        // Initialize database connection
        DatabaseManager dbManager = new DatabaseManager();
        try {
            // Connect to database
            dbManager.connect();

            // Create schema
            System.out.println("Creating database schema...");
            dbManager.createSchema();

            // Import CSV data
            System.out.println("Starting CSV import...");
            CsvImporter importer = new CsvImporter(dbManager.getConnection());
            importer.importCsv(csvFilePath);

            // Create indices after data is imported (for better performance)
            System.out.println("Creating database indices...");
            dbManager.createIndices();

            System.out.println("Import completed successfully.");
            System.out.println("Total rows processed: " + importer.getProcessedRows());

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure database connection is closed
            try {
                dbManager.disconnect();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}