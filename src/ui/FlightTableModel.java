package ui;

import model.Flight;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for displaying flight data in a JTable.
 */
public class FlightTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {
            "Date", "Airline", "Flight #", "Origin", "Destination",
            "Scheduled Dep.", "Actual Dep.", "Scheduled Arr.", "Actual Arr.", "Delay (min)"
    };

    private List<Flight> flights = new ArrayList<>();

    /**
     * Sets the flights to display in the table.
     * @param flights the flights to display
     */
    public void setFlights(List<Flight> flights) {
        this.flights = flights;
        fireTableDataChanged();
    }

    /**
     * Gets the flight at the specified row index.
     * @param rowIndex the row index
     * @return the flight
     */
    public Flight getFlightAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < flights.size()) {
            return flights.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return flights.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 9) { // Delay column
            return Integer.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= flights.size()) {
            return null;
        }

        Flight flight = flights.get(rowIndex);

        switch (columnIndex) {
            case 0: return flight.getFormattedDate();
            case 1: return flight.getAirlineName();
            case 2: return flight.getFullFlightNumber();
            case 3: return flight.getOriginDisplay();
            case 4: return flight.getDestinationDisplay();
            case 5: return Flight.formatTime(flight.getScheduledDeparture());
            case 6: return Flight.formatTime(flight.getActualDeparture());
            case 7: return Flight.formatTime(flight.getScheduledArrival());
            case 8: return Flight.formatTime(flight.getActualArrival());
            case 9: return flight.getDelayMinutes();
            default: return null;
        }
    }
}