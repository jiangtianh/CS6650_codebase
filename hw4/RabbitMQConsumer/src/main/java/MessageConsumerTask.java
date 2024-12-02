import JSONClasses.LiftRide;
import JSONClasses.LiftRidePostRequest;
import JSONClasses.ResultRecord;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.google.gson.Gson;
import com.rabbitmq.client.DeliverCallback;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MessageConsumerTask implements Runnable {

    private static final String QUEUE_NAME = "liftRidesExchange";
    private final BlockingDeque<ResultRecord> resultQueue;
    private final Connection connection;
    private final Gson gson;
    private final ScheduledExecutorService scheduler;

    public MessageConsumerTask(Connection connection, BlockingDeque<ResultRecord> resultQueue, ScheduledExecutorService scheduler) {
        this.resultQueue = resultQueue;
        this.connection = connection;
        this.gson = new Gson();
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        try {
            final Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicQos(30);

            List<Long> deliveryTags = new ArrayList<>();


            Runnable ackTask = () -> {
                if (!deliveryTags.isEmpty()) {
                    long lastDeliveryTag = deliveryTags.get(deliveryTags.size() - 1);
                    try {
                        channel.basicAck(lastDeliveryTag, true);
                        deliveryTags.clear();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            scheduler.scheduleWithFixedDelay(ackTask, 30, 10, TimeUnit.SECONDS);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                long messageStartTime = System.currentTimeMillis();
                String messageJson = new String(delivery.getBody(), "UTF-8");
                storeLiftRide(messageJson);

                deliveryTags.add(delivery.getEnvelope().getDeliveryTag());
                if (deliveryTags.size() >= 20) {
                    long lastDeliveryTag = deliveryTags.get(deliveryTags.size() - 1);
                    channel.basicAck(lastDeliveryTag, true);
                    deliveryTags.clear();
                }

                long messageEndTime = System.currentTimeMillis();
                this.resultQueue.add(new ResultRecord(messageStartTime, messageEndTime));
            };

            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void storeLiftRide(String messageJson) {
        try (Jedis jedis = RedisConnection.getJedis()) {
            if (jedis == null) {
                System.err.println("Failed to get Redis connection");
                return;
            }

            LiftRidePostRequest liftRide = gson.fromJson(messageJson, LiftRidePostRequest.class);
            
            String skierId = String.valueOf(liftRide.getSkierId());
            String resortId = String.valueOf(liftRide.getResortId());
            String seasonId = String.valueOf(liftRide.getSeasonId());
            String dayId = String.valueOf(liftRide.getDayId());
            String liftId = String.valueOf(liftRide.getLiftId());
            int vertical = liftRide.getLiftId() * 10;

            String resortKey = "resort:" + resortId;
            String dayField = "season:" + seasonId + ":day:" + dayId;
            String skierSeasonKey = "skier:" + skierId + ":resort:" + resortId + ":season:" + seasonId;
            String skierResortKey = "skier:" + skierId + ":resort:" + resortId;
            String seasonField = "season:" + seasonId;

            Pipeline pipeline = jedis.pipelined();

            pipeline.sadd(resortKey + ":" + dayField, skierId);
            pipeline.rpush(skierSeasonKey + ":day:" + dayId, liftId);
            pipeline.hincrBy(skierResortKey, seasonField, vertical);
            pipeline.sync();

        } catch (Exception e) {
            System.err.println("Error storing lift ride: " + e.getMessage());
            e.printStackTrace();
        }
    }
}