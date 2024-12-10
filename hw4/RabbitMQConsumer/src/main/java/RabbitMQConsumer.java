
import JSONClasses.ResultRecord;
import ResultHandling.ResultHandler;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RabbitMQConsumer {
    public static final int NUM_THREADS = 32;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public static void main(String[] args) throws Exception {

        ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
        BlockingDeque<ResultRecord> resultQueue = new LinkedBlockingDeque<>();
        String RabbitMQIp = "35.155.53.0";

        String ResultCsvFilePath = "result.csv";

        System.out.println("Starting consumer... with" + NUM_THREADS + " threads");
        System.out.println("RabbitMQ IP: " + RabbitMQIp);
        System.out.println("Redis IP: " + RedisConnection.REDIS_HOST);

        ConnectionFactory factory = new ConnectionFactory();

        // Set the RabbitMQ IP
//        factory.setHost("localhost");
        factory.setHost(RabbitMQIp);
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");

        Connection connection = factory.newConnection();


        for (int i = 0; i < NUM_THREADS; i++) {
            threadPool.execute(
                    new MessageConsumerTask(connection, resultQueue, scheduler)
            );
        }

        Thread csvWriterThread = new Thread(new CSVWriterTask(resultQueue, ResultCsvFilePath));
        csvWriterThread.setPriority(Thread.MIN_PRIORITY);
        csvWriterThread.start();

    }
}