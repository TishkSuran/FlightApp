package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a flight with all associated data.
 */
public class Flight {

    private int flightId;
    private LocalDate date;
    private String airlineCode;
    private String airlineName;
    private int flightNumber;
    private String originCode;
    private String originCity;
    private String destCode;
    private String destCity;
    private int scheduledDeparture;
    private int actualDeparture;
    private int scheduledArrival;
    private int actualArrival;
    private List<Delay> delays = new ArrayList<>();

    /**
     * Creates a new Flight instance.
     */
    public Flight() {
        // Default constructor
    }

    /**
     * Returns the full flight number (airline code + number).
     * @return the full flight number
     */
    public String getFullFlightNumber() {
        return airlineCode + flightNumber;
    }

    /**
     * Returns the delay in minutes for this flight, or 0 if it arrived early or on time.
     * @return delay in minutes
     */
    public int getDelayMinutes() {
        if (actualArrival == 0 || scheduledArrival == 0) {
            return 0;
        }

        // Convert times to minutes since midnight for easier comparison
        int scheduledArrivalMinutes = timeToMinutes(scheduledArrival);
        int actualArrivalMinutes = timeToMinutes(actualArrival);

        // Calculate difference, considering midnight crossings
        int diffMinutes = actualArrivalMinutes - scheduledArrivalMinutes;
        if (diffMinutes < -720) { // More than 12 hours negative -> must have crossed midnight
            diffMinutes += 1440; // Add 24 hours in minutes
        } else if (diffMinutes > 720) { // More than 12 hours positive -> scheduled must have crossed midnight
            diffMinutes -= 1440; // Subtract 24 hours in minutes
        }

        return Math.max(0, diffMinutes); // Negative means early arrival, count as 0 delay
    }

    /**
     * Converts a time in HHMM format to minutes since midnight.
     * @param time the time in HHMM format
     * @return minutes since midnight
     */
    private int timeToMinutes(int time) {
        int hours = time / 100;
        int minutes = time % 100;
        return hours * 60 + minutes;
    }

    /**
     * Formats a time in HHMM format to HH:MM.
     * @param time the time in HHMM format
     * @return formatted time string
     */
    public static String formatTime(int time) {
        if (time == 0) {
            return "N/A";
        }
        int hours = time / 100;
        int minutes = time % 100;
        return String.format("%02d:%02d", hours, minutes);
    }

    /**
     * Represents a delay reason and duration.
     */
    public static class Delay {
        private final String reason;
        private final int minutes;

        public Delay(String reason, int minutes) {
            this.reason = reason;
            this.minutes = minutes;
        }

        public String getReason() {
            return reason;
        }

        public int getMinutes() {
            return minutes;
        }

        public String getFormattedReason() {
            switch (reason) {
                case "CARRIER": return "Airline";
                case "WEATHER": return "Weather";
                case "NAS": return "Air Traffic Control";
                case "SECURITY": return "Security";
                case "LATE_AIRCRAFT": return "Late Aircraft";
                default: return reason;
            }
        }
    }

    // Getters and setters
    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDateFromString(String dateStr) {
        if (dateStr != null && dateStr.length() == 8) {
            // Parse from YYYYMMDD format
            try {
                this.date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            } catch (Exception e) {
                System.err.println("Invalid date format: " + dateStr);
            }
        }
    }

    public String getFormattedDate() {
        return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
    }

    public String getAirlineCode() {
        return airlineCode;
    }

    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    public int getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(int flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getOriginCode() {
        return originCode;
    }

    public void setOriginCode(String originCode) {
        this.originCode = originCode;
    }

    public String getOriginCity() {
        return originCity;
    }

    public void setOriginCity(String originCity) {
        this.originCity = originCity;
    }

    public String getDestCode() {
        return destCode;
    }

    public void setDestCode(String destCode) {
        this.destCode = destCode;
    }

    public String getDestCity() {
        return destCity;
    }

    public void setDestCity(String destCity) {
        this.destCity = destCity;
    }

    public int getScheduledDeparture() {
        return scheduledDeparture;
    }

    public void setScheduledDeparture(int scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
    }

    public int getActualDeparture() {
        return actualDeparture;
    }

    public void setActualDeparture(int actualDeparture) {
        this.actualDeparture = actualDeparture;
    }

    public int getScheduledArrival() {
        return scheduledArrival;
    }

    public void setScheduledArrival(int scheduledArrival) {
        this.scheduledArrival = scheduledArrival;
    }

    public int getActualArrival() {
        return actualArrival;
    }

    public void setActualArrival(int actualArrival) {
        this.actualArrival = actualArrival;
    }

    public List<Delay> getDelays() {
        return delays;
    }

    public void addDelay(Delay delay) {
        this.delays.add(delay);
    }

    public void setDelays(List<Delay> delays) {
        this.delays = delays;
    }

    public String getOriginDisplay() {
        return originCode + " - " + originCity;
    }

    public String getDestinationDisplay() {
        return destCode + " - " + destCity;
    }

    @Override
    public String toString() {
        return getFullFlightNumber() + " from " + originCode + " to " + destCode + " on " + getFormattedDate();
    }
}