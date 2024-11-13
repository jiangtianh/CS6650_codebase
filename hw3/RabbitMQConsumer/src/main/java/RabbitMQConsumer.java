
import JSONClasses.ResultRecord;
import ResultHandling.ResultHandler;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RabbitMQConsumer {
    public static final int NUM_THREADS = 35;

    public static void main(String[] args) throws Exception {

        ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
        BlockingDeque<ResultRecord> resultQueue = new LinkedBlockingDeque<>();

        System.out.println("Starting consumer... with" + NUM_THREADS + " threads");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
//        factory.setHost("35.155.53.0");
//        factory.setPort(5672);
//        factory.setUsername("admin");
//        factory.setPassword("admin");

        Connection connection = factory.newConnection();


        for (int i = 0; i < NUM_THREADS; i++) {
            threadPool.execute(
                    new MessageConsumerTask(connection, resultQueue)
            );
        }

        Thread csvWriterThread = new Thread(new CSVWriterTask(resultQueue, "result.csv"));
        csvWriterThread.setPriority(Thread.MIN_PRIORITY);
        csvWriterThread.start();

    }
}