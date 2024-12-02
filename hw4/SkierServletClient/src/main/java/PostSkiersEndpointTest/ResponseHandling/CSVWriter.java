package PostSkiersEndpointTest.ResponseHandling;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;

public class CSVWriter {
    public static void write(String filePath, BlockingDeque<ResponseRecord> resultQueue) {
        int totalResults = resultQueue.size();

        try (FileWriter writer = new FileWriter(filePath, false)) { // 'false' to overwrite the file
            writer.append("StartTimeStamp,Latency,RequestType,ResponseCode\n");
            for (int i = 0; i < totalResults; i++) {
                ResponseRecord responseRecord = resultQueue.take();
                long reqStartTime = responseRecord.getStartTimeStamp();
                long latency = responseRecord.getEndTimeStamp() - reqStartTime;
                String requestType = responseRecord.getRequestType();
                int responseCode = responseRecord.getResponseCode();
                writer.append(String.valueOf(reqStartTime)).append(",").append(String.valueOf(latency)).append(",").append(requestType).append(",").append(String.valueOf(responseCode)).append("\n");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
