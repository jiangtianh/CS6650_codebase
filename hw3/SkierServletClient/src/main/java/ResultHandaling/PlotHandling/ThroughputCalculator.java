package ResultHandaling.PlotHandling;

import java.util.ArrayList;
import java.util.List;

public class ThroughputCalculator {
    public static List<Double> calculateThroughput(List<Long[]> timestamps, long intervalMillis) {
        List<Double> throughput = new ArrayList<>();
        long testStartTime = timestamps.get(0)[0];
        long testEndTime = timestamps.get(timestamps.size() - 1)[1];
        int idx = 0;

        for (long time = testStartTime; time <= testEndTime; time += intervalMillis) {
            int count = 0;
            while (idx < timestamps.size() && timestamps.get(idx)[1] < time) {
                count += 1;
                idx += 1;
            }
            throughput.add((double) count / (intervalMillis / 1000));
        }
        return throughput;
    }

}
