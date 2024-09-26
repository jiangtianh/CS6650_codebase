package ResultHandaling;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    public static List<ResultRecord> read(String filePath) {
        List<ResultRecord> resultRecords = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // skip the header
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                long startTime = Long.parseLong(values[0]);
                int latency = Integer.parseInt(values[1]);
                String requestType = values[2];
                int responseCode = Integer.parseInt(values[3]);
                resultRecords.add(new ResultRecord(startTime, latency, requestType, responseCode));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resultRecords;
    }


}
