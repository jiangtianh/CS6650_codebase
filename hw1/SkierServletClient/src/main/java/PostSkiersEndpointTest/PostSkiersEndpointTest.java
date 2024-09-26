package PostSkiersEndpointTest;

import PostSkiersEndpointTest.ResponseHandling.CSVWriter;
import PostSkiersEndpointTest.ResponseHandling.ResponseRecord;
import PostSkiersEndpointTest.RequestsGeneration.EventGenerator;
import PostSkiersEndpointTest.RequestsGeneration.RequestUrlAndJson;


import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class PostSkiersEndpointTest {


    private static final String CSV_PATH = "result.csv";
    private static final String BASE_URL = "http://34.222.55.214:8080/JavaServlets_war/skiers";

    private static final int TOTAL_REQUESTS = 200000;
    private static final int INITIAL_THREADS = 32;
    private static final int REQUESTS_PER_INITIAL_TREAD = 1000;

    private static final int LaterThreads = 168;
    private static final int LaterRequestPerThread = (TOTAL_REQUESTS - (INITIAL_THREADS * REQUESTS_PER_INITIAL_TREAD)) / LaterThreads;


    private static final BlockingDeque<RequestUrlAndJson> eventQueue = new LinkedBlockingDeque<>();
    private static final BlockingDeque<ResponseRecord> resultQueue = new LinkedBlockingDeque<>();
    private static final AtomicInteger successCount = new AtomicInteger(0);



    public static void startTest() throws InterruptedException {

        Thread requestGeneratorThread = new Thread(new EventGenerator(eventQueue, TOTAL_REQUESTS, BASE_URL));
        requestGeneratorThread.start();

        ExecutorService executorService = Executors.newFixedThreadPool(INITIAL_THREADS + LaterThreads);


        long startTime = System.currentTimeMillis();

        for (int i = 0; i < INITIAL_THREADS; i++) {
            executorService.execute(new PostWorker(REQUESTS_PER_INITIAL_TREAD, eventQueue, successCount, resultQueue));
        }

        for (int i = 0; i < LaterThreads; i++) {
            executorService.execute(new PostWorker(LaterRequestPerThread, eventQueue, successCount, resultQueue));
        }

        requestGeneratorThread.join();

        executorService.shutdown();
        executorService.awaitTermination(100, java.util.concurrent.TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        System.out.println("Total threads: " + (INITIAL_THREADS + LaterThreads));
        System.out.println("Each thread after the initial 32 is sending out: " + LaterRequestPerThread + " requests");
        System.out.println("Success count: " + successCount);
        System.out.println("Unsuccessful count: " + (TOTAL_REQUESTS - successCount.get()));
        System.out.println("Time taken: " + (endTime - startTime) + "ms");
        System.out.println("Throughput: " + (successCount.get() / ((endTime - startTime) / 1000.0)));

        CSVWriter.write(CSV_PATH, resultQueue);
    }
}
