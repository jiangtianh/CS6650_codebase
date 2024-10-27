package ResultHandling;

import JSONClasses.ResultRecord;
import ResultHandling.PlotHandling.ThroughputCalculator;
import ResultHandling.PlotHandling.ThroughputPlotter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;

public class ResultHandler {
    private final BlockingDeque<ResultRecord> resultQueue;

    public ResultHandler(BlockingDeque<ResultRecord> resultQueue) {
        this.resultQueue = resultQueue;
    }

    public void showResult() {
        processBasicInfo();
        plotThroughput();
    }

    private void processBasicInfo() {
        int totalResults = resultQueue.size();
        System.out.println("====================== Starting to process results ======================");
        System.out.println("Total results: " + totalResults);

        List<Long> responseTimes = new ArrayList<>();

        for (ResultRecord result : resultQueue) {
            responseTimes.add(result.getEndTimeStamp() - result.getStartTimeStamp());
        }

        Collections.sort(responseTimes);
        long minResponseTime = responseTimes.get(0);
        long maxResponseTime = responseTimes.get(responseTimes.size() - 1);
        long meanResponseTime = responseTimes.stream().mapToLong(Long::longValue).sum() / totalResults;
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
        long initialStartTime = Long.MAX_VALUE;
        long finalEndTime = Long.MIN_VALUE;

        for (ResultRecord result : this.resultQueue) {
            long startTime = result.getStartTimeStamp();
            long endTime = result.getEndTimeStamp();

            if (startTime < initialStartTime) {
                initialStartTime = startTime;
            }
            if (endTime > finalEndTime) {
                finalEndTime = endTime;
            }
            timestamps.add(new Long[]{startTime, endTime});
        }

        List<Double> throughput = ThroughputCalculator.calculateThroughput(timestamps, 1000, initialStartTime, finalEndTime);

        System.out.println("Throughput: " + this.resultQueue.size() / ((finalEndTime - initialStartTime) / 1000.0));

        ThroughputPlotter.plotThroughput(throughput);
    }

}
