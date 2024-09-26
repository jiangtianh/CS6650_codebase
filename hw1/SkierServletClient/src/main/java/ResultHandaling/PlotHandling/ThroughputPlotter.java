package ResultHandaling.PlotHandling;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.util.List;

public class ThroughputPlotter extends JFrame {

    public ThroughputPlotter(String title, List<Double> throughputData) {
        super(title);

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Throughput Over Time",
                "Time (s)",
                "Throughput (req/s)",
                createDataset(throughputData),
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);
    }

    private DefaultCategoryDataset createDataset(List<Double> throughputData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < throughputData.size(); i++) {
            dataset.addValue(throughputData.get(i), "Throughput", Integer.toString(i));
        }
        return dataset;
    }

    public static void plotThroughput(List<Double> throughputData) {
        ThroughputPlotter chart = new ThroughputPlotter("Throughput Over Time", throughputData);
        chart.setSize(800, 600);
        chart.setLocationRelativeTo(null);
        chart.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        chart.setVisible(true);
    }

}
