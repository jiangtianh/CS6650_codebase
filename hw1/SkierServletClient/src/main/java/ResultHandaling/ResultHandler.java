package ResultHandaling;

import ResultHandaling.PlotHandling.ThroughputCalculator;
import ResultHandaling.PlotHandling.ThroughputPlotter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultHandler {
    private final List<ResultRecord> resultRecord;

    public ResultHandler(String csvPath) {
        this.resultRecord = CSVReader.read("result.csv");;
    }

    public void showResult() {
        processBasicInfo();
        plotThroughput();

    }


    private void processBasicInfo() {
        int totalResults = resultRecord.size();
        System.out.println("====================== Starting to process results ======================");
        System.out.println("Total results: " + totalResults);

        List<Integer> responseTimes = new ArrayList<>();

        for (ResultRecord result : resultRecord) {
            responseTimes.add(result.getLatency());
        }

        Collections.sort(responseTimes);
        long minResponseTime = responseTimes.get(0);
        long maxResponseTime = responseTimes.get(responseTimes.size() - 1);
        long meanResponseTime = responseTimes.stream().mapToInt(Integer::intValue).sum() / totalResults;
        long medianResponseTime;
        if (totalResults % 2 == 0) {
            medianResponseTime = (responseTimes.get(totalResults / 2) + responseTimes.get(totalResults / 2 - 1)) / 2;
        } else {
            medianResponseTime = responseTimes.get(totalResults / 2);
        }
        long p99ResponseTime = responseTimes.get((int) (totalResults * 0.99));

        System.out.println("Min response time: " + minResponseTime + " ms");
        System.out.println("Max response time: " + maxResponseTime + " ms");
        System.out.println("Mean response time: " + meanResponseTime + " ms");
        System.out.println("Median response time: " + medianResponseTime + " ms");
        System.out.println("99th percentile response time: " + p99ResponseTime + " ms");
    }

    private void plotThroughput() {
        List<Long[]> timestamps = new ArrayList<>();
        for (ResultRecord result : resultRecord) {
            long startTime = result.getStartTimeStamp();
            long endTime = startTime + result.getLatency();
            timestamps.add(new Long[]{startTime, endTime});
        }
        List<Double> throughput = ThroughputCalculator.calculateThroughput(timestamps, 1000);

        ThroughputPlotter.plotThroughput(throughput);
    }
}
