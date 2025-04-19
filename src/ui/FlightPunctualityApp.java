package ui;

import model.Flight;
import service.FlightDataService;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Main application frame for the Flight Punctuality Desktop Application.
 */
public class FlightPunctualityApp extends JFrame {

    private final FlightDataService dataService;
    private final FlightTableModel tableModel;
    private final JTable flightTable;
    private final FlightDetailPanel detailPanel;
    private final SearchPanel searchPanel;
    private final AnalysisPanel analysisPanel;
    private final JLabel statusLabel;

    /**
     * Creates and initializes the main application frame.
     * @throws SQLException if a database access error occurs
     */
    public FlightPunctualityApp() throws SQLException {
        super("Flight Punctuality Application");

        // Set up data service
        dataService = new FlightDataService();

        // Set up main components
        tableModel = new FlightTableModel();
        flightTable = new JTable(tableModel);
        detailPanel = new FlightDetailPanel();
        searchPanel = new SearchPanel(
                dataService.getAirlines(),
                dataService.getAirports(),
                this::handleSearch,
                this::handleClear
        );
        analysisPanel = new AnalysisPanel();
        statusLabel = new JLabel("Ready");

        // Configure the UI
        configureUI();

        // Add a window listener to handle cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    dataService.disconnect();
                } catch (SQLException ex) {
                    System.err.println("Error disconnecting from database: " + ex.getMessage());
                }
            }
        });

        // Set up the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    /**
     * Configures the user interface components.
     */
    private void configureUI() {
        // Set up flight table
        flightTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flightTable.setAutoCreateRowSorter(true);

        // Add row selection listener to show details
        flightTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = flightTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = flightTable.convertRowIndexToModel(selectedRow);
                    Flight selectedFlight = tableModel.getFlightAt(modelRow);
                    detailPanel.setFlight(selectedFlight);
                } else {
                    detailPanel.clearDetails();
                }
            }
        });

        // Create table sorter
        TableRowSorter<FlightTableModel> sorter = new TableRowSorter<>(tableModel);
        flightTable.setRowSorter(sorter);

        // Create scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(flightTable);

        // Create main panel with split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(800);

        // Left panel: search panel and table
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Right panel: detail panel and analysis panel with tabbed layout
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(detailPanel, BorderLayout.NORTH);
        rightPanel.add(analysisPanel, BorderLayout.CENTER);

        // Add panels to split pane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        // Create analysis menu
        JMenuBar menuBar = new JMenuBar();
        JMenu analysisMenu = new JMenu("Analysis");

        JMenuItem airlineAnalysisItem = new JMenuItem("Airline Delays by Year");
        airlineAnalysisItem.addActionListener(this::handleAirlineAnalysis);
        analysisMenu.add(airlineAnalysisItem);

        JMenuItem airportAnalysisItem = new JMenuItem("Airport Delays by Year");
        airportAnalysisItem.addActionListener(this::handleAirportAnalysis);
        analysisMenu.add(airportAnalysisItem);

        JMenuItem timeSeriesItem = new JMenuItem("Airport Delays Over Time");
        timeSeriesItem.addActionListener(this::handleTimeSeriesAnalysis);
        analysisMenu.add(timeSeriesItem);

        menuBar.add(analysisMenu);
        setJMenuBar(menuBar);

        // Add status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        // Add components to frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);
    }

    /**
     * Handles search button click event.
     * @param e the action event
     */
    private void handleSearch(ActionEvent e) {
        try {
            statusLabel.setText("Searching...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Get search criteria
            String airline = searchPanel.getAirline();
            String flightNumber = searchPanel.getFlightNumber();
            String origin = searchPanel.getOrigin();
            String destination = searchPanel.getDestination();
            LocalDate startDate = searchPanel.getStartDate();
            LocalDate endDate = searchPanel.getEndDate();
            Integer minDelay = searchPanel.getMinDelay();
            Integer maxDelay = searchPanel.getMaxDelay();
            String delayReason = searchPanel.getDelayReason();

            // Perform search
            List<Flight> results = dataService.searchFlights(
                    airline, flightNumber, origin, destination,
                    startDate, endDate, minDelay, maxDelay, delayReason
            );

            // Update table
            tableModel.setFlights(results);

            // Clear selection
            flightTable.clearSelection();

            // Update status
            statusLabel.setText("Found " + results.size() + " matching flights");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Search Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Search failed");
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Handles clear button click event.
     * @param e the action event
     */
    private void handleClear(ActionEvent e) {
        searchPanel.clearFields();
        tableModel.setFlights(new java.util.ArrayList<>());
        detailPanel.clearDetails();
        statusLabel.setText("Ready");
    }

    /**
     * Handles airline analysis menu item click event.
     * @param e the action event
     */
    private void handleAirlineAnalysis(ActionEvent e) {
        String yearStr = JOptionPane.showInputDialog(
                this,
                "Enter year for analysis:",
                "Airline Delay Analysis",
                JOptionPane.QUESTION_MESSAGE
        );

        if (yearStr != null && !yearStr.trim().isEmpty()) {
            try {
                int year = Integer.parseInt(yearStr);

                if (year < 2019 || year > 2023) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Please enter a year between 2019 and 2023",
                            "Invalid Year",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                statusLabel.setText("Analyzing airline delays for " + year + "...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Get data
                Map<String, Double> data = dataService.getAverageDelayByAirline(year);

                // Show chart
                analysisPanel.showAirlineDelayChart(data, year);

                statusLabel.setText("Airline delay analysis completed for " + year);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid year",
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE
                );
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Database error: " + ex.getMessage(),
                        "Analysis Error",
                        JOptionPane.ERROR_MESSAGE
                );
                statusLabel.setText("Analysis failed");
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    /**
     * Handles airport analysis menu item click event.
     * @param e the action event
     */
    private void handleAirportAnalysis(ActionEvent e) {
        String yearStr = JOptionPane.showInputDialog(
                this,
                "Enter year for analysis:",
                "Airport Delay Analysis",
                JOptionPane.QUESTION_MESSAGE
        );

        if (yearStr != null && !yearStr.trim().isEmpty()) {
            try {
                int year = Integer.parseInt(yearStr);

                if (year < 2019 || year > 2023) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Please enter a year between 2019 and 2023",
                            "Invalid Year",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                statusLabel.setText("Analyzing airport delays for " + year + "...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Get data
                Map<String, Double> data = dataService.getAverageDelayByAirport(year);

                // Show chart
                analysisPanel.showAirportDelayChart(data, year);

                statusLabel.setText("Airport delay analysis completed for " + year);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid year",
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE
                );
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Database error: " + ex.getMessage(),
                        "Analysis Error",
                        JOptionPane.ERROR_MESSAGE
                );
                statusLabel.setText("Analysis failed");
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    /**
     * Handles time series analysis menu item click event.
     * @param e the action event
     */
    private void handleTimeSeriesAnalysis(ActionEvent e) {
        // Get airport
        try {
            List<String> airports = dataService.getAirports();
            String[] airportOptions = airports.toArray(new String[0]);

            String selectedAirport = (String) JOptionPane.showInputDialog(
                    this,
                    "Select airport:",
                    "Airport Delay Time Series",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    airportOptions,
                    airportOptions[0]
            );

            if (selectedAirport != null && !selectedAirport.trim().isEmpty()) {
                // Extract airport code
                String airportCode = selectedAirport.substring(0, selectedAirport.indexOf(" - "));
                String airportName = selectedAirport.substring(selectedAirport.indexOf(" - ") + 3);

                statusLabel.setText("Analyzing delays for " + airportName + " over time...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Get data
                Map<String, Double> data = dataService.getDelaysByMonth(airportCode, 2019, 2023);

                // Show chart
                analysisPanel.showTimeSeriesChart(data, airportName);

                statusLabel.setText("Time series analysis completed for " + airportName);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Database error: " + ex.getMessage(),
                    "Analysis Error",
                    JOptionPane.ERROR_MESSAGE
            );
            statusLabel.setText("Analysis failed");
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}