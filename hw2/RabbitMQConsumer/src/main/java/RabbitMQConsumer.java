import JSONClasses.LiftRide;
import JSONClasses.ResultRecord;
import ResultHandling.ResultHandler;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RabbitMQConsumer {

    private static final int NUM_THREADS = 75;
    private static final int TOTAL_MESSAGES = 200000;

    public static void main(String[] args) throws Exception {

        CountDownLatch latch = new CountDownLatch(TOTAL_MESSAGES);
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        BlockingDeque<ResultRecord> resultQueue = new LinkedBlockingDeque<>();
        ConcurrentHashMap<String, LiftRide> messageMap = new ConcurrentHashMap<>();
        AtomicBoolean isFirstMessageReceived = new AtomicBoolean(false);
        long[] startTime = new long[1];

        System.out.println("Starting consumer...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("35.155.53.0");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");

        Connection connection = factory.newConnection();


        for (int i = 0; i < NUM_THREADS; i++) {
            threadPool.execute(
                    new MessageConsumerTask(connection, messageMap, resultQueue, latch, isFirstMessageReceived, startTime)
            );
        }


        threadPool.shutdown();
        latch.await();

        long endTime = System.currentTimeMillis();
        System.out.println("Total time taken: " + (endTime - startTime[0]) + "ms");
        System.out.println("Total messages: " + (TOTAL_MESSAGES - latch.getCount()));
        System.out.println("Throughput: " + (TOTAL_MESSAGES / ((endTime - startTime[0]) / 1000.0)));
        connection.close();

        ResultHandler resultHandler = new ResultHandler(resultQueue);
        resultHandler.showResult();
    }
}