package ResultHandling;

import JSONClasses.ResultRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ResultProcessor {
    public static void main(String[] args) {
        String filePath = "result.csv";
        BlockingDeque<ResultRecord> resultQueue = new LinkedBlockingDeque<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                long startTimeStamp = Long.parseLong(values[0]);
                long endTimeStamp = Long.parseLong(values[1]);
                resultQueue.add(new ResultRecord(startTimeStamp, endTimeStamp));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ResultHandler resultHandler = new ResultHandler(resultQueue);
        resultHandler.showResult();
    }
}
