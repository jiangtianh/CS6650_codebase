package ResultHandling.PlotHandling;

import java.util.ArrayList;
import java.util.List;

public class ThroughputCalculator {
    public static List<Double> calculateThroughput(List<Long[]> timestamps, long intervalMillis, long testStartTime, long testEndTime) {

        List<Double> throughput = new ArrayList<>();

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
