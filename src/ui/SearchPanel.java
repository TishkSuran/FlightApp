package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel for entering flight search criteria.
 */
public class SearchPanel extends JPanel {

    private final JComboBox<String> airlineComboBox;
    private final JTextField flightNumberField;
    private final JComboBox<String> originComboBox;
    private final JComboBox<String> destinationComboBox;

    private final JSpinner startDateSpinner;
    private final JSpinner endDateSpinner;

    private final JSpinner minDelaySpinner;
    private final JSpinner maxDelaySpinner;
    private final JComboBox<String> delayReasonComboBox;

    private final JButton searchButton;
    private final JButton clearButton;

    /**
     * Creates a new search panel.
     * @param airlines list of airlines for dropdown
     * @param airports list of airports for dropdown
     * @param searchListener listener for search button clicks
     * @param clearListener listener for clear button clicks
     */
    public SearchPanel(List<String> airlines, List<String> airports,
                       ActionListener searchListener, ActionListener clearListener) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Search Flights"));

        // Create search form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        // Airline field
        c.gridx = 0;
        c.gridy = 0;
        formPanel.add(new JLabel("Airline:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        airlineComboBox = new JComboBox<>();
        airlineComboBox.setEditable(true);
        // Add empty option
        airlineComboBox.addItem("");
        // Add airlines
        for (String airline : airlines) {
            airlineComboBox.addItem(airline);
        }
        formPanel.add(airlineComboBox, c);

        // Flight number field
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        formPanel.add(new JLabel("Flight Number:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        flightNumberField = new JTextField();
        formPanel.add(flightNumberField, c);

        // Origin field
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.0;
        formPanel.add(new JLabel("Origin:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        originComboBox = new JComboBox<>();
        originComboBox.setEditable(true);
        // Add empty option
        originComboBox.addItem("");
        // Add airports
        for (String airport : airports) {
            originComboBox.addItem(airport);
        }
        formPanel.add(originComboBox, c);

        // Destination field
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0.0;
        formPanel.add(new JLabel("Destination:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        destinationComboBox = new JComboBox<>();
        destinationComboBox.setEditable(true);
        // Add empty option
        destinationComboBox.addItem("");
        // Add airports
        for (String airport : airports) {
            destinationComboBox.addItem(airport);
        }
        formPanel.add(destinationComboBox, c);

        // Date range fields
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0.0;
        formPanel.add(new JLabel("Start Date:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        // Date spinner with custom date editor
        SpinnerDateModel startDateModel = new SpinnerDateModel();
        startDateSpinner = new JSpinner(startDateModel);
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startDateEditor);
        formPanel.add(startDateSpinner, c);

        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 0.0;
        formPanel.add(new JLabel("End Date:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        SpinnerDateModel endDateModel = new SpinnerDateModel();
        endDateSpinner = new JSpinner(endDateModel);
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endDateEditor);
        formPanel.add(endDateSpinner, c);

        // Delay fields
        c.gridx = 0;
        c.gridy = 6;
        c.weightx = .0;
        formPanel.add(new JLabel("Min Delay (min):"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        SpinnerNumberModel minDelayModel = new SpinnerNumberModel(0, -120, 1440, 5);
        minDelaySpinner = new JSpinner(minDelayModel);
        formPanel.add(minDelaySpinner, c);

        c.gridx = 0;
        c.gridy = 7;
        c.weightx = 0.0;
        formPanel.add(new JLabel("Max Delay (min):"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        SpinnerNumberModel maxDelayModel = new SpinnerNumberModel(0, 0, 1440, 5);
        maxDelaySpinner = new JSpinner(maxDelayModel);
        formPanel.add(maxDelaySpinner, c);

        c.gridx = 0;
        c.gridy = 8;
        c.weightx = 0.0;
        formPanel.add(new JLabel("Delay Reason:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        delayReasonComboBox = new JComboBox<>(new String[]{
                "", "CARRIER", "WEATHER", "NAS", "SECURITY", "LATE_AIRCRAFT"
        });
        formPanel.add(delayReasonComboBox, c);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchButton = new JButton("Search");
        searchButton.addActionListener(searchListener);
        buttonPanel.add(searchButton);

        clearButton = new JButton("Clear");
        clearButton.addActionListener(clearListener);
        buttonPanel.add(clearButton);

        // Add panels to main panel
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Clears all search fields.
     */
    public void clearFields() {
        airlineComboBox.setSelectedIndex(0);
        flightNumberField.setText("");
        originComboBox.setSelectedIndex(0);
        destinationComboBox.setSelectedIndex(0);
        startDateSpinner.setValue(new java.util.Date());
        endDateSpinner.setValue(new java.util.Date());
        minDelaySpinner.setValue(0);
        maxDelaySpinner.setValue(0);
        delayReasonComboBox.setSelectedIndex(0);
    }

    /**
     * Gets the selected airline or empty if none selected.
     * @return airline code or name
     */
    public String getAirline() {
        String airline = (String) airlineComboBox.getSelectedItem();
        if (airline == null || airline.isEmpty()) {
            return null;
        }

        // Extract airline code if a selection was made from dropdown
        if (airline.contains(" - ")) {
            return airline.substring(0, airline.indexOf(" - "));
        }

        return airline;
    }

    /**
     * Gets the entered flight number or empty if none entered.
     * @return flight number
     */
    public String getFlightNumber() {
        return flightNumberField.getText().trim();
    }

    /**
     * Gets the selected origin airport or empty if none selected.
     * @return origin airport code or name
     */
    public String getOrigin() {
        String origin = (String) originComboBox.getSelectedItem();
        if (origin == null || origin.isEmpty()) {
            return null;
        }

        // Extract airport code if a selection was made from dropdown
        if (origin.contains(" - ")) {
            return origin.substring(0, origin.indexOf(" - "));
        }

        return origin;
    }

    /**
     * Gets the selected destination airport or empty if none selected.
     * @return destination airport code or name
     */
    public String getDestination() {
        String destination = (String) destinationComboBox.getSelectedItem();
        if (destination == null || destination.isEmpty()) {
            return null;
        }

        // Extract airport code if a selection was made from dropdown
        if (destination.contains(" - ")) {
            return destination.substring(0, destination.indexOf(" - "));
        }

        return destination;
    }

    /**
     * Gets the selected start date or null if none selected.
     * @return start date
     */
    public LocalDate getStartDate() {
        Object value = startDateSpinner.getValue();
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        }
        return null;
    }

    /**
     * Gets the selected end date or null if none selected.
     * @return end date
     */
    public LocalDate getEndDate() {
        Object value = endDateSpinner.getValue();
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        }
        return null;
    }

    /**
     * Gets the minimum delay or null if not specified.
     * @return minimum delay in minutes
     */
    public Integer getMinDelay() {
        int minDelay = (Integer) minDelaySpinner.getValue();
        if (minDelay == 0) {
            return null;
        }
        return minDelay;
    }

    /**
     * Gets the maximum delay or null if not specified.
     * @return maximum delay in minutes
     */
    public Integer getMaxDelay() {
        int maxDelay = (Integer) maxDelaySpinner.getValue();
        if (maxDelay == 0) {
            return null;
        }
        return maxDelay;
    }

    /**
     * Gets the selected delay reason or null if none selected.
     * @return delay reason code
     */
    public String getDelayReason() {
        String reason = (String) delayReasonComboBox.getSelectedItem();
        if (reason == null || reason.isEmpty()) {
            return null;
        }
        return reason;
    }
}