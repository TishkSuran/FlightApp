package ui;

import model.Flight;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for displaying detailed information about a flight.
 */
public class FlightDetailPanel extends JPanel {

    private final JLabel dateLabel = new JLabel();
    private final JLabel flightNumberLabel = new JLabel();
    private final JLabel airlineLabel = new JLabel();
    private final JLabel originLabel = new JLabel();
    private final JLabel destinationLabel = new JLabel();
    private final JLabel scheduledDepLabel = new JLabel();
    private final JLabel actualDepLabel = new JLabel();
    private final JLabel scheduledArrLabel = new JLabel();
    private final JLabel actualArrLabel = new JLabel();
    private final JLabel delayLabel = new JLabel();
    private final JPanel delayReasonPanel = new JPanel(new GridLayout(0, 1));

    /**
     * Creates a new flight detail panel.
     */
    public FlightDetailPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Flight Details"));

        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 5));

        infoPanel.add(new JLabel("Date:"));
        infoPanel.add(dateLabel);

        infoPanel.add(new JLabel("Flight Number:"));
        infoPanel.add(flightNumberLabel);

        infoPanel.add(new JLabel("Airline:"));
        infoPanel.add(airlineLabel);

        infoPanel.add(new JLabel("Origin:"));
        infoPanel.add(originLabel);

        infoPanel.add(new JLabel("Destination:"));
        infoPanel.add(destinationLabel);

        infoPanel.add(new JLabel("Scheduled Departure:"));
        infoPanel.add(scheduledDepLabel);

        infoPanel.add(new JLabel("Actual Departure:"));
        infoPanel.add(actualDepLabel);

        infoPanel.add(new JLabel("Scheduled Arrival:"));
        infoPanel.add(scheduledArrLabel);

        infoPanel.add(new JLabel("Actual Arrival:"));
        infoPanel.add(actualArrLabel);

        infoPanel.add(new JLabel("Delay (minutes):"));
        infoPanel.add(delayLabel);

        // Delay reasons section
        JPanel delaySection = new JPanel(new BorderLayout());
        delaySection.add(new JLabel("Delay Reasons:"), BorderLayout.NORTH);
        delaySection.add(delayReasonPanel, BorderLayout.CENTER);

        add(infoPanel, BorderLayout.NORTH);
        add(delaySection, BorderLayout.CENTER);

        // Initial state
        clearDetails();
    }

    /**
     * Displays the details of the specified flight.
     * @param flight the flight to display
     */
    public void setFlight(Flight flight) {
        if (flight == null) {
            clearDetails();
            return;
        }

        dateLabel.setText(flight.getFormattedDate());
        flightNumberLabel.setText(flight.getFullFlightNumber());
        airlineLabel.setText(flight.getAirlineName());
        originLabel.setText(flight.getOriginDisplay());
        destinationLabel.setText(flight.getDestinationDisplay());
        scheduledDepLabel.setText(Flight.formatTime(flight.getScheduledDeparture()));
        actualDepLabel.setText(Flight.formatTime(flight.getActualDeparture()));
        scheduledArrLabel.setText(Flight.formatTime(flight.getScheduledArrival()));
        actualArrLabel.setText(Flight.formatTime(flight.getActualArrival()));

        int delayMinutes = flight.getDelayMinutes();
        delayLabel.setText(String.valueOf(delayMinutes));

        // Format delay label based on value
        if (delayMinutes > 60) {
            delayLabel.setForeground(Color.RED);
        } else if (delayMinutes > 15) {
            delayLabel.setForeground(Color.ORANGE);
        } else {
            delayLabel.setForeground(Color.GREEN);
        }

// Display delay reasons
        delayReasonPanel.removeAll();
        if (flight.getDelays().isEmpty()) {
            delayReasonPanel.add(new JLabel("No specific delay reasons recorded"));
        } else {
            for (Flight.Delay delay : flight.getDelays()) {
                JLabel reasonLabel = new JLabel(delay.getFormattedReason() + ": " + delay.getMinutes() + " minutes");
                delayReasonPanel.add(reasonLabel);
            }
        }

        revalidate();
        repaint();
    }

    /**
     * Clears all flight details from the panel.
     */
    public void clearDetails() {
        dateLabel.setText("N/A");
        flightNumberLabel.setText("N/A");
        airlineLabel.setText("N/A");
        originLabel.setText("N/A");
        destinationLabel.setText("N/A");
        scheduledDepLabel.setText("N/A");
        actualDepLabel.setText("N/A");
        scheduledArrLabel.setText("N/A");
        actualArrLabel.setText("N/A");
        delayLabel.setText("N/A");
        delayLabel.setForeground(Color.BLACK);

        delayReasonPanel.removeAll();
        delayReasonPanel.add(new JLabel("No flight selected"));

        revalidate();
        repaint();
    }
}