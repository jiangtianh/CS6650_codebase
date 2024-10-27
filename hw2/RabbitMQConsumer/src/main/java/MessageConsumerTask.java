import JSONClasses.LiftRide;
import JSONClasses.LiftRidePostRequest;
import JSONClasses.ResultRecord;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.google.gson.Gson;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class MessageConsumerTask implements Runnable {

    private static final String QUEUE_NAME = "liftRidesExchange";

    private final ConcurrentHashMap<String, LiftRide> messageMap;
    private final BlockingDeque<ResultRecord> resultQueue;
    private final CountDownLatch latch;
    private final Connection connection;
    private final Gson gson;
    private final AtomicBoolean isFirstMessageReceived;
    private final long[] startTime;

    public MessageConsumerTask(Connection connection, ConcurrentHashMap<String, LiftRide> messageMap, BlockingDeque<ResultRecord> resultQueue, CountDownLatch latch, AtomicBoolean isFirstMessageReceived, long[] startTime) {
        this.messageMap = messageMap;
        this.resultQueue = resultQueue;
        this.connection = connection;
        this.latch = latch;
        this.gson = new Gson();
        this.isFirstMessageReceived = isFirstMessageReceived;
        this.startTime = startTime;
    }

    @Override
    public void run() {
        try {
            final Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                if (isFirstMessageReceived.compareAndSet(false, true)) {
                    startTime[0] = System.currentTimeMillis();
                }

                long messageStartTime = System.currentTimeMillis();
                String messageJson = new String(delivery.getBody(), "UTF-8");
                storeLiftRide(messageJson);
                long messageEndTime = System.currentTimeMillis();
                this.resultQueue.add(new ResultRecord(messageStartTime, messageEndTime));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                this.latch.countDown();
            };

            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void storeLiftRide(String messageJson) {
        LiftRidePostRequest message = gson.fromJson(messageJson, LiftRidePostRequest.class);

        String key = message.getResortID() + "/" +
                message.getSeasonID() + "/" +
                message.getDayID() + "/" +
                message.getSkierID();

        this.messageMap.put(key, message.getLiftRide());
    }
}