package PostSkiersEndpointTest;

import PostSkiersEndpointTest.ResponseHandling.CSVWriter;
import PostSkiersEndpointTest.ResponseHandling.ResponseRecord;
import PostSkiersEndpointTest.RequestsGeneration.EventGenerator;
import PostSkiersEndpointTest.RequestsGeneration.RequestUrlAndJson;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PostSkiersEndpointTest {


    private static final String CSV_PATH = "result.csv";
    private static final String BASE_URL = "http://LoadBalancer-481638051.us-west-2.elb.amazonaws.com/JavaServlets_war/skiers";
//    private static final String BASE_URL = "http://localhost:8080/JavaServlets_war_exploded/skiers";
//    private static final String BASE_URL = "http://34.209.109.225:8080/JavaServlets_war/skiers";

    private static final int TOTAL_REQUESTS = 600000;
    private static final int INITIAL_THREADS = 32;
    private static final int REQUESTS_PER_INITIAL_TREAD = 1000;

    private static final int LaterThreads = 64;
    private static final int RAMP_UP_INTERVAL_MS = 500;


    private static final BlockingDeque<RequestUrlAndJson> eventQueue = new LinkedBlockingDeque<>();
    private static final BlockingDeque<ResponseRecord> resultQueue = new LinkedBlockingDeque<>();
    private static final AtomicInteger successCount = new AtomicInteger(0);

    public static void startTest() throws InterruptedException {

        Thread requestGeneratorThread = new Thread(new EventGenerator(eventQueue, TOTAL_REQUESTS, BASE_URL));
        requestGeneratorThread.start();

        ExecutorService initialExecutor = Executors.newFixedThreadPool(INITIAL_THREADS);
        ExecutorService laterExecutor = Executors.newFixedThreadPool(LaterThreads);

        // Countdown latch to wait for any initial threads to finish, before starting the later threads
        CountDownLatch latch = new CountDownLatch(1);

        long startTime = System.currentTimeMillis();

        HttpClient client = new HttpClient();
        EventCountCircuitBreaker circuitBreaker = new EventCountCircuitBreaker(4800, 1, TimeUnit.SECONDS, 4300);

        for (int i = 0; i < INITIAL_THREADS; i++) {
            initialExecutor.submit(() -> {
                try {
                    new PostWorker(REQUESTS_PER_INITIAL_TREAD, eventQueue, successCount, resultQueue, circuitBreaker).run();
                } finally {
                    latch.countDown();
                }
                return null;
            });
//
        }

        latch.await();
        long firstThreadEndTime = System.currentTimeMillis();
        System.out.println("First thread from initial 32 took: " + (firstThreadEndTime - startTime) + "ms");

        for (int i = 0; i < LaterThreads; i++) {
            laterExecutor.execute(new PostWorkerWithoutSpecifiedRequestNumber(eventQueue, successCount, resultQueue, circuitBreaker));
        }

        requestGeneratorThread.join();

        initialExecutor.shutdown();
        laterExecutor.shutdown();
        initialExecutor.awaitTermination(300, java.util.concurrent.TimeUnit.SECONDS);
        laterExecutor.awaitTermination(300, java.util.concurrent.TimeUnit.SECONDS);


        long endTime = System.currentTimeMillis();
        System.out.println("Total threads: " + (INITIAL_THREADS + LaterThreads));
        System.out.println("Success count: " + successCount);
        System.out.println("Unsuccessful count: " + (TOTAL_REQUESTS - successCount.get()));
        System.out.println("Time taken: " + (endTime - startTime) + "ms");
        System.out.println("Throughput: " + (successCount.get() / ((endTime - startTime) / 1000.0)));

        CSVWriter.write(CSV_PATH, resultQueue);
    }
}
