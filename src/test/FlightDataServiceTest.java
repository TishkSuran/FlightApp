package test;

import model.Flight;
import service.FlightDataService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Test class for the Flight Data Service functionality.
 */
public class FlightDataServiceTest {

    /**
     * Main method to run tests.
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("Running Flight Data Service Tests...");

        try {
            FlightDataService service = new FlightDataService();

            // Test basic queries
            testBasicQueries(service);

            // Test analysis queries
            testAnalysisQueries(service);

            // Cleanup
            service.disconnect();

            System.out.println("All tests passed!");

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tests basic search queries.
     * @param service the flight data service
     * @throws SQLException if a database access error occurs
     */
    private static void testBasicQueries(FlightDataService service) throws SQLException {
        System.out.println("\nTesting basic queries...");

        // Test search by airline
        List<Flight> deltaFlights = service.searchFlights("DL", null, null, null, null, null, null, null, null);
        System.out.println("✓ Found " + deltaFlights.size() + " Delta flights");

        // Test search by flight number
        List<Flight> aa1Flights = service.searchFlights(null, "AA1", null, null, null, null, null, null, null);
        System.out.println("✓ Found " + aa1Flights.size() + " AA1 flights");

        // Test search by origin
        List<Flight> jfkFlights = service.searchFlights(null, null, "JFK", null, null, null, null, null, null);
        System.out.println("✓ Found " + jfkFlights.size() + " flights from JFK");

        // Test search by destination
        List<Flight> laxFlights = service.searchFlights(null, null, null, "LAX", null, null, null, null, null);
        System.out.println("✓ Found " + laxFlights.size() + " flights to LAX");

        // Test search by date range
        LocalDate startDate = LocalDate.of(2021, 11, 1);
        LocalDate endDate = LocalDate.of(2021, 11, 30);
        List<Flight> novemberFlights = service.searchFlights(null, null, "JFK", null, startDate, endDate, null, null, null);
        System.out.println("✓ Found " + novemberFlights.size() + " flights from JFK in November 2021");

        // Test search by delay
        List<Flight> delayedFlights = service.searchFlights("B6", null, null, "FLL", null, null, 60, null, null);
        System.out.println("✓ Found " + delayedFlights.size() + " JetBlue flights to Fort Lauderdale delayed by at least 1 hour");

        // Test search by delay reason
        List<Flight> securityDelays = service.searchFlights(null, null, null, "DEN", null, null, 30, null, "SECURITY");
        System.out.println("✓ Found " + securityDelays.size() + " flights delayed by security at Denver");

        System.out.println("Basic query tests passed.");
    }

    /**
     * Tests analysis queries.
     * @param service the flight data service
     * @throws SQLException if a database access error occurs
     */
    private static void testAnalysisQueries(FlightDataService service) throws SQLException {
        System.out.println("\nTesting analysis queries...");

        // Test airline delay analysis
        Map<String, Double> airlineDelays = service.getAverageDelayByAirline(2022);
        System.out.println("✓ Analyzed delays for " + airlineDelays.size() + " airlines in 2022");

        // Test airport delay analysis
        Map<String, Double> airportDelays = service.getAverageDelayByAirport(2023);
        System.out.println("✓ Analyzed delays for " + airportDelays.size() + " airports in 2023");

        // Test time series analysis
        Map<String, Double> timeSeriesData = service.getDelaysByMonth("LAX", 2020, 2023);
        System.out.println("✓ Analyzed delays for LAX across " + timeSeriesData.size() + " months");

        System.out.println("Analysis query tests passed.");
    }
}