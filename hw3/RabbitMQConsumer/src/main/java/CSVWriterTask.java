import JSONClasses.ResultRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;

public class CSVWriterTask implements Runnable {
    private final BlockingDeque<ResultRecord> resultQueue;
    private final String filePath;

    public CSVWriterTask(BlockingDeque<ResultRecord> resultQueue, String filePath) {
        this.resultQueue = resultQueue;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        // Clear the previous content of the file
        try (FileWriter writer = new FileWriter(filePath, false)) {
            writer.write("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileWriter writer = new FileWriter(filePath, true)) {
            while (true) {
                ResultRecord record = resultQueue.take();
                writer.write(record.getStartTimeStamp() + "," + record.getEndTimeStamp() + "\n");
                writer.flush();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
