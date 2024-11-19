package PostSkiersEndpointTest;

import PostSkiersEndpointTest.RequestsGeneration.RequestUrlAndJson;
import PostSkiersEndpointTest.ResponseHandling.ResponseRecord;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PostWorker implements Runnable {

    private static final int MAX_RETRY = 5;
    private final int requestNumber;
    private final BlockingDeque<RequestUrlAndJson> queue;
    private final AtomicInteger successfulRequests;
    private final BlockingDeque<ResponseRecord> resultQueue;
    private final EventCountCircuitBreaker circuitBreaker;

    public PostWorker(int requestNumber,
                      BlockingDeque<RequestUrlAndJson> queue,
                      AtomicInteger successfulRequests,
                      BlockingDeque<ResponseRecord> resultQueue,
                      EventCountCircuitBreaker circuitBreaker) {
        this.requestNumber = requestNumber;
        this.queue = queue;
        this.successfulRequests = successfulRequests;
        this.resultQueue = resultQueue;

        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public void run() {
        HttpMethodRetryHandler retryHandler = (method, exception, executionCount) -> {
            if (executionCount >= MAX_RETRY) {
                return false;
            }
            if (exception != null) {
                return true;
            }
            int statusCode = method.getStatusCode();
            return statusCode >= 400;
        };
        HttpClient client = new HttpClient();


        try {
            int processedRequests = 0;
            while (processedRequests < requestNumber) {
                if (circuitBreaker.checkState()) {
                    RequestUrlAndJson requestUrlAndJson = queue.take();
                    sendPostRequest(client, retryHandler, requestUrlAndJson);
                    processedRequests++;
                    circuitBreaker.incrementAndCheckState();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void sendPostRequest(HttpClient client, HttpMethodRetryHandler retryHandler, RequestUrlAndJson requestUrlAndJson) throws IOException {
        PostMethod postRequest = new PostMethod(requestUrlAndJson.getUrl());
        postRequest.setRequestEntity(new StringRequestEntity(requestUrlAndJson.getJson(), "application/json", "UTF-8"));
        postRequest.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);

        long startTime = System.currentTimeMillis();
        int statusCode = client.executeMethod(postRequest);
        if (statusCode == 201) {
            long endTime = System.currentTimeMillis();
            resultQueue.add(new ResponseRecord(startTime, endTime, "POST", statusCode));
            postRequest.releaseConnection();
            successfulRequests.incrementAndGet();
            return;
        } else {
            System.out.println("Request failed: " + statusCode);
        }

        postRequest.releaseConnection();
        return;
    }

}
