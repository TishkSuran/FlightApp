package ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Panel for displaying analysis charts and reports.
 */
public class AnalysisPanel extends JPanel {

    private final JTabbedPane tabbedPane;
    private final JPanel airlinePanel;
    private final JPanel airportPanel;
    private final JPanel timeSeriesPanel;

    /**
     * Creates a new analysis panel.
     */
    public AnalysisPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Analysis and Reports"));

        tabbedPane = new JTabbedPane();

        // Create panels for each analysis type
        airlinePanel = new JPanel(new BorderLayout());
        airportPanel = new JPanel(new BorderLayout());
        timeSeriesPanel = new JPanel(new BorderLayout());

        // Add tabs
        tabbedPane.addTab("By Airline", airlinePanel);
        tabbedPane.addTab("By Airport", airportPanel);
        tabbedPane.addTab("Over Time", timeSeriesPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Displays a chart of average delay by airline.
     * @param data map of airline names to average delay
     * @param year the year of the analysis
     */
    public void showAirlineDelayChart(Map<String, Double> data, int year) {
        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Add data to dataset, sorted by delay in descending order
        data.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> dataset.addValue(entry.getValue(), "Delay", entry.getKey()));

        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Average Delay by Airline in " + year,  // Title
                "Airline",               // X-Axis Label
                "Average Delay (minutes)", // Y-Axis Label
                dataset,                 // Dataset
                PlotOrientation.HORIZONTAL, // Orientation
                false,                   // Show Legend
                true,                    // Use tooltips
                false                    // Generate URLs
        );

        // Customize chart
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new Color(79, 129, 189));

        // Remove space between bars
        renderer.setItemMargin(0.0);

        // Create chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));

        // Add to panel
        airlinePanel.removeAll();
        airlinePanel.add(chartPanel, BorderLayout.CENTER);
        airlinePanel.revalidate();
        airlinePanel.repaint();

        // Select tab
        tabbedPane.setSelectedComponent(airlinePanel);
    }

    /**
     * Displays a chart of average delay by airport.
     * @param data map of airport names to average delay
     * @param year the year of the analysis
     */
    public void showAirportDelayChart(Map<String, Double> data, int year) {
        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Add data to dataset, sorted by delay in descending order
        data.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> dataset.addValue(entry.getValue(), "Delay", entry.getKey()));

        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Average Delay by Departure Airport in " + year,  // Title
                "Airport",               // X-Axis Label
                "Average Delay (minutes)", // Y-Axis Label
                dataset,                 // Dataset
                PlotOrientation.HORIZONTAL, // Orientation
                false,                   // Show Legend
                true,                    // Use tooltips
                false                    // Generate URLs
        );

        // Customize chart
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new Color(192, 80, 77));

        // Rotate category labels for better readability
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.25);

        // Create chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));

        // Add to panel
        airportPanel.removeAll();
        airportPanel.add(chartPanel, BorderLayout.CENTER);
        airportPanel.revalidate();
        airportPanel.repaint();

        // Select tab
        tabbedPane.setSelectedComponent(airportPanel);
    }

    /**
     * Displays a chart of average delay over time for a specific airport.
     * @param data map of month-year to average delay
     * @param airportName the name of the airport
     */
    public void showTimeSeriesChart(Map<String, Double> data, String airportName) {
        // Create dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Average Delay");

        // Add data to series in chronological order
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    // Parse month/year to get a numeric x value
                    String[] parts = entry.getKey().split("/");
                    if (parts.length == 2) {
                        try {
                            int month = Integer.parseInt(parts[0]);
                            int year = Integer.parseInt(parts[1]);
                            // Convert to a decimal year value (e.g., 2020.5 for June 2020)
                            double xValue = year + (month - 1) / 12.0;
                            series.add(xValue, entry.getValue());
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid month/year: " + entry.getKey());
                        }
                    }
                });

        dataset.addSeries(series);

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Average Delay for Flights Departing " + airportName,  // Title
                "Date",                  // X-Axis Label
                "Average Delay (minutes)", // Y-Axis Label
                dataset,                 // Dataset
                PlotOrientation.VERTICAL, // Orientation
                false,                   // Show Legend
                true,                    // Use tooltips
                false                    // Generate URLs
        );

        // Customize chart
        chart.getPlot().setBackgroundPaint(Color.WHITE);

        // Create chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));

        // Add to panel
        timeSeriesPanel.removeAll();
        timeSeriesPanel.add(chartPanel, BorderLayout.CENTER);
        timeSeriesPanel.revalidate();
        timeSeriesPanel.repaint();

        // Select tab
        tabbedPane.setSelectedComponent(timeSeriesPanel);
    }
}