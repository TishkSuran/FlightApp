package database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles importing CSV flight data into the SQLite database.
 */
public class CsvImporter {

    private static final int BATCH_SIZE = 1000;
    private static final int PROGRESS_INTERVAL = 10000;

    private final Connection connection;
    private final Map<String, Integer> airportIds = new HashMap<>();
    private final Map<String, Integer> airlineIds = new HashMap<>();

    private int processedRows = 0;

    /**
     * Creates a new CSV importer using the given database connection.
     * @param connection the database connection
     */
    public CsvImporter(Connection connection) {
        this.connection = connection;
    }

    /**
     * Imports flight data from the specified CSV file.
     * @param csvFilePath the path to the CSV file
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database access error occurs
     */
    public void importCsv(String csvFilePath) throws IOException, SQLException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            // Read header line to get column indices
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }

            String[] headers = parseCsvLine(headerLine);
            Map<String, Integer> columnMap = mapColumnIndices(headers);

            // Prepare statements for inserting data
            PreparedStatement airlineStmt = connection.prepareStatement(
                    "INSERT OR IGNORE INTO Airline (iata_code, name) VALUES (?, ?)"
            );

            PreparedStatement airportStmt = connection.prepareStatement(
                    "INSERT OR IGNORE INTO Airport (iata_code, name) VALUES (?, ?)"
            );

            PreparedStatement flightStmt = connection.prepareStatement(
                    "INSERT INTO Flight (date, airline_code, flight_number, flight_origin, " +
                            "flight_destination, scheduled_departure, actual_departure, " +
                            "scheduled_arrival, actual_arrival) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS
            );

            PreparedStatement delayStmt = connection.prepareStatement(
                    "INSERT INTO Delay_Reason (flight_id, reason, delay_length) VALUES (?, ?, ?)"
            );

            // Process data rows
            String line;
            int batchCount = 0;

            while ((line = reader.readLine()) != null) {
                try {
                    String[] data = parseCsvLine(line);

                    // Skip rows that don't have enough data
                    if (data.length < getMinRequiredColumns(columnMap)) {
                        System.out.println("Skipping row: insufficient columns in line " + (processedRows + 1));
                        continue;
                    }

                    // Get values from the CSV row
                    String flDate = getColumnValue(data, columnMap, "FL_DATE").replace("-", "");
                    String airlineName = getColumnValue(data, columnMap, "AIRLINE");
                    String airlineCode = getColumnValue(data, columnMap, "AIRLINE_CODE");
                    String flNumberStr = getColumnValue(data, columnMap, "FL_NUMBER");
                    String origin = getColumnValue(data, columnMap, "ORIGIN");
                    String originCity = getColumnValue(data, columnMap, "ORIGIN_CITY");
                    String dest = getColumnValue(data, columnMap, "DEST");
                    String destCity = getColumnValue(data, columnMap, "DEST_CITY");
                    String crsDepTimeStr = getColumnValue(data, columnMap, "CRS_DEP_TIME");
                    String depTimeStr = getColumnValue(data, columnMap, "DEP_TIME");
                    String crsArrTimeStr = getColumnValue(data, columnMap, "CRS_ARR_TIME");
                    String arrTimeStr = getColumnValue(data, columnMap, "ARR_TIME");
                    String cancelledStr = getColumnValue(data, columnMap, "CANCELLED");

                    // Skip if essential data is missing
                    if (isEmptyOrNull(airlineCode) || isEmptyOrNull(origin) || isEmptyOrNull(dest)) {
                        System.out.println("Skipping row: missing essential data in line " + (processedRows + 1));
                        continue;
                    }

                    // Skip cancelled flights if needed
                    if (!isEmptyOrNull(cancelledStr) && !cancelledStr.equals("0.0") && !cancelledStr.equals("0")) {
                        System.out.println("Skipping cancelled flight in line " + (processedRows + 1));
                        continue;
                    }

                    // Insert airline if not already processed
                    airlineStmt.setString(1, airlineCode.trim());
                    airlineStmt.setString(2, airlineName.trim());
                    airlineStmt.executeUpdate();

                    // Insert origin airport if not already processed
                    airportStmt.setString(1, origin.trim());
                    airportStmt.setString(2, originCity.trim());
                    airportStmt.executeUpdate();

                    // Insert destination airport if not already processed
                    airportStmt.setString(1, dest.trim());
                    airportStmt.setString(2, destCity.trim());
                    airportStmt.executeUpdate();

                    // Insert flight data
                    flightStmt.setString(1, flDate);
                    flightStmt.setString(2, airlineCode.trim());

                    try {
                        flightStmt.setInt(3, Integer.parseInt(flNumberStr.trim()));
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid flight number at line " + (processedRows + 1) + ": " + flNumberStr);
                        continue;
                    }

                    flightStmt.setString(4, origin.trim());
                    flightStmt.setString(5, dest.trim());

                    // Handle scheduled and actual times
                    flightStmt.setInt(6, parseTimeValue(crsDepTimeStr));
                    flightStmt.setInt(7, parseTimeValue(depTimeStr));
                    flightStmt.setInt(8, parseTimeValue(crsArrTimeStr));
                    flightStmt.setInt(9, parseTimeValue(arrTimeStr));

                    flightStmt.executeUpdate();

                    // Get the generated flight_id
                    int flightId = -1;
                    try (var rs = flightStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            flightId = rs.getInt(1);
                        } else {
                            System.out.println("Failed to get flight ID for row " + (processedRows + 1));
                            continue;
                        }
                    }

                    // Insert delay reasons if present
                    insertDelayReason(delayStmt, flightId, "CARRIER", getColumnValue(data, columnMap, "DELAY_DUE_CARRIER"));
                    insertDelayReason(delayStmt, flightId, "WEATHER", getColumnValue(data, columnMap, "DELAY_DUE_WEATHER"));
                    insertDelayReason(delayStmt, flightId, "NAS", getColumnValue(data, columnMap, "DELAY_DUE_NAS"));
                    insertDelayReason(delayStmt, flightId, "SECURITY", getColumnValue(data, columnMap, "DELAY_DUE_SECURITY"));
                    insertDelayReason(delayStmt, flightId, "LATE_AIRCRAFT", getColumnValue(data, columnMap, "DELAY_DUE_LATE_AIRCRAFT"));

                    // Commit in batches for better performance
                    batchCount++;
                    if (batchCount >= BATCH_SIZE) {
                        connection.commit();
                        batchCount = 0;
                    }

                    processedRows++;
                    if (processedRows % PROGRESS_INTERVAL == 0) {
                        System.out.println("Processed " + processedRows + " rows");
                    }
                } catch (Exception e) {
                    System.out.println("Error processing row " + (processedRows + 1) + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Final commit for any remaining batches
            if (batchCount > 0) {
                connection.commit();
            }

            System.out.println("Import completed. Processed " + processedRows + " rows.");
        }
    }

    /**
     * Maps column names to their indices.
     * @param headers the CSV header row
     * @return map of column names to indices
     */
    private Map<String, Integer> mapColumnIndices(String[] headers) {
        Map<String, Integer> columnMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            columnMap.put(headers[i].trim(), i);
        }
        return columnMap;
    }

    /**
     * Gets the minimum number of columns required based on mapped indices.
     * @param columnMap the column name to index map
     * @return the minimum number of columns required
     */
    private int getMinRequiredColumns(Map<String, Integer> columnMap) {
        int maxIndex = 0;
        String[] requiredColumns = {"FL_DATE", "AIRLINE_CODE", "FL_NUMBER", "ORIGIN", "DEST"};

        for (String column : requiredColumns) {
            Integer index = columnMap.get(column);
            if (index != null && index > maxIndex) {
                maxIndex = index;
            }
        }

        return maxIndex + 1;
    }

    /**
     * Gets a column value safely from a data row.
     * @param data the data row
     * @param columnMap the column name to index map
     * @param columnName the column name
     * @return the column value or empty string if not found
     */
    private String getColumnValue(String[] data, Map<String, Integer> columnMap, String columnName) {
        Integer index = columnMap.get(columnName);
        if (index == null || index >= data.length) {
            return "";
        }
        return data[index].trim();
    }

    /**
     * Checks if a string is null or empty.
     * @param str the string to check
     * @return true if null or empty
     */
    private boolean isEmptyOrNull(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Parses a time value from string, handling various formats.
     * @param timeStr the time string
     * @return the time as an integer in HHMM format
     */
    private int parseTimeValue(String timeStr) {
        if (isEmptyOrNull(timeStr)) {
            return 0;
        }

        // Remove any decimal part if present
        if (timeStr.contains(".")) {
            timeStr = timeStr.substring(0, timeStr.indexOf('.'));
        }

        try {
            // Try parsing directly
            return Integer.parseInt(timeStr);
        } catch (NumberFormatException e) {
            try {
                // Try parsing as a time (HH:MM)
                if (timeStr.contains(":")) {
                    String[] parts = timeStr.split(":");
                    int hours = Integer.parseInt(parts[0]);
                    int minutes = Integer.parseInt(parts[1]);
                    return hours * 100 + minutes;
                }
            } catch (Exception ex) {
                System.out.println("Invalid time format: " + timeStr);
            }
            return 0;
        }
    }

    /**
     * Parses a CSV line respecting quoted fields and escaped commas.
     * @param line the CSV line to parse
     * @return array of parsed values
     */
    private String[] parseCsvLine(String line) {
        // Simple CSV parsing that handles quoted fields
        // Note: This is a simplified parser. A real application might use a library like OpenCSV

        if (line == null || line.isEmpty()) {
            return new String[0];
        }

        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        java.util.List<String> fields = new java.util.ArrayList<>();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    field.append('"');
                    i++; // Skip the next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of field
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }

        // Add the last field
        fields.add(field.toString());

        return fields.toArray(new String[0]);
    }

    /**
     * Inserts a delay reason if the delay value is valid.
     * @param stmt the prepared statement for delay reason insertion
     * @param flightId the flight ID
     * @param reason the delay reason
     * @param delayStr the delay length as string
     * @throws SQLException if a database access error occurs
     */
    private void insertDelayReason(PreparedStatement stmt, int flightId, String reason, String delayStr)
            throws SQLException {
        if (delayStr != null && !delayStr.trim().isEmpty()) {
            try {
                float delay = Float.parseFloat(delayStr.trim());
                if (delay > 0) {
                    int delayMinutes = Math.round(delay);
                    stmt.setInt(1, flightId);
                    stmt.setString(2, reason);
                    stmt.setInt(3, delayMinutes);
                    stmt.executeUpdate();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid delay value for " + reason + ": " + delayStr);
            }
        }
    }

    /**
     * Gets the total number of rows processed.
     * @return the count of processed rows
     */
    public int getProcessedRows() {
        return processedRows;
    }
}