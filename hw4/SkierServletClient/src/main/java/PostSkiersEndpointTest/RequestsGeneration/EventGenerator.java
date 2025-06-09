package PostSkiersEndpointTest.RequestsGeneration;

import java.util.Random;
import java.util.concurrent.BlockingDeque;

public class EventGenerator implements Runnable{
    private final Random random = new Random();
    private final BlockingDeque<RequestUrlAndJson> queue;
    private final int totalRequests;
    private final String baseUrl;

    public EventGenerator(BlockingDeque<RequestUrlAndJson> queue, int totalRequests, String baseUrl) {
        this.queue = queue;
        this.totalRequests = totalRequests;
        this.baseUrl = baseUrl;
    }

    @Override
    public void run() {
        for (int i = 0; i < totalRequests; i++) {
            int skierID = i % 100000 + 1;
            int resortID = 1;
            int liftID = random.nextInt(40) + 1;
            int seasonID = 2024;
            int dayID = (i / 200000) + 1;
            int time = random.nextInt(360) + 1;


            RequestJsonObj requestBody = new RequestJsonObj(time, liftID);
            String url = baseUrl + "/" + resortID + "/seasons/" + seasonID + "/days/" + dayID + "/skiers/" + skierID;
            RequestUrlAndJson requestUrlAndJson = new RequestUrlAndJson(url, requestBody);
            queue.add(requestUrlAndJson);
        }
    }
}
