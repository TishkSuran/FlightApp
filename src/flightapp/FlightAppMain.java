package flightapp;

import ui.FlightPunctualityApp;

import javax.swing.*;
import java.sql.SQLException;

/**
 * Main class for the Flight Punctuality Application.
 */
public class FlightAppMain {

    /**
     * Main entry point for the application.
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }

        // Launch application on the event dispatch thread
        SwingUtilities.invokeLater(() -> {
            try {
                FlightPunctualityApp app = new FlightPunctualityApp();
                app.setVisible(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Database error: " + e.getMessage() + "\n\n" +
                                "Make sure the flights.db database exists and is accessible.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}