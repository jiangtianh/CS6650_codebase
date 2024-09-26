

import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;


import PostSkiersEndpointTest.PostWorker;
import PostSkiersEndpointTest.RequestsGeneration.EventGenerator;
import PostSkiersEndpointTest.RequestsGeneration.RequestUrlAndJson;
import PostSkiersEndpointTest.ResponseHandling.CSVWriter;
import PostSkiersEndpointTest.ResponseHandling.ResponseRecord;
import ResultHandaling.ResultHandler;


public class TestLatency {
    private static final int TOTAL_REQUESTS = 10000;
    private static final String BASE_URL = "http://52.88.120.229:8080/JavaServlets_war/skiers";

    public static void main(String[] args) throws IOException, InterruptedException {

        BlockingDeque<RequestUrlAndJson> eventQueue = new LinkedBlockingDeque<>();
        BlockingDeque<ResponseRecord> resultQueue = new LinkedBlockingDeque<>();

        Thread requestGeneratorThread = new Thread(new EventGenerator(eventQueue, TOTAL_REQUESTS, BASE_URL));
        requestGeneratorThread.start();
        requestGeneratorThread.join();


        AtomicInteger successCount = new AtomicInteger(0);

        ExecutorService fixedThreadExecutor = Executors.newFixedThreadPool(1);

        long startTime = System.currentTimeMillis();

        fixedThreadExecutor.execute(new PostWorker(TOTAL_REQUESTS, eventQueue, successCount, resultQueue));

        fixedThreadExecutor.shutdown();
        fixedThreadExecutor.awaitTermination(300, java.util.concurrent.TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();

        System.out.println("Successful requests: " + successCount);
        System.out.println("Failed requests: " + (TOTAL_REQUESTS - successCount.get()));
        System.out.println("Time taken: " + (endTime - startTime) + "ms");

        System.out.println("Throughput: " + (TOTAL_REQUESTS / ((endTime - startTime) / 1000.0)));

        CSVWriter.write("result.csv", resultQueue);
        ResultHandler resultHandler = new ResultHandler("result.csv");
        resultHandler.showResult();
    }
}
