package ResultHandling.PlotHandling;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ThroughputPlotter {
    public static final String THROUGHPUT_PLOT_PATH = "throughput_plot.png";
    private JFreeChart lineChart;

    public ThroughputPlotter(List<Double> throughputData) {
        this.lineChart = ChartFactory.createLineChart(
                "Consumer Throughput Over Time",
                "Time (s)",
                "Throughput (req/s)",
                createDataset(throughputData),
                PlotOrientation.VERTICAL,
                true, true, false);
    }

    private DefaultCategoryDataset createDataset(List<Double> throughputData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < throughputData.size(); i++) {
            dataset.addValue(throughputData.get(i), "Throughput", Integer.toString(i));
        }
        return dataset;
    }

    public static void plotThroughput(List<Double> throughputData) {
        ThroughputPlotter chart = new ThroughputPlotter(throughputData);
        try {
            ChartUtils.saveChartAsPNG(new File(THROUGHPUT_PLOT_PATH), chart.lineChart, 800, 600);
            System.out.println("Plot saved as " + THROUGHPUT_PLOT_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}