import JSONClasses.LiftRide;
import JSONClasses.ResultRecord;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.google.gson.Gson;
import com.rabbitmq.client.DeliverCallback;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;


public class MessageConsumerTask implements Runnable {

    private static final String QUEUE_NAME = "liftRidesExchange";
    private final BlockingDeque<ResultRecord> resultQueue;
    private final Connection connection;
    private final Gson gson;

    public MessageConsumerTask(Connection connection, BlockingDeque<ResultRecord> resultQueue) {
        this.resultQueue = resultQueue;
        this.connection = connection;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try {
            final Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicQos(10);

            List<Long> deliveryTags = new ArrayList<>();

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                long messageStartTime = System.currentTimeMillis();
                String messageJson = new String(delivery.getBody(), "UTF-8");
                storeLiftRide(messageJson);

                deliveryTags.add(delivery.getEnvelope().getDeliveryTag());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

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

            LiftRide liftRide = gson.fromJson(messageJson, LiftRide.class);
            String skierId = String.valueOf(liftRide.getSkierId());
            String resortId = String.valueOf(liftRide.getResortId());
            String seasonId = String.valueOf(liftRide.getSeasonId());
            String dayId = String.valueOf(liftRide.getDayId());
            String liftId = String.valueOf(liftRide.getLiftId());
            int vertical = liftRide.getLiftId() * 10;

            String resortKey = "resort:" + resortId;
            String dayField = "season:" + seasonId + ":day:" + dayId;
            jedis.sadd(resortKey + ":" + dayField, skierId);

            String skierSeasonKey = "skier:" + skierId + ":season:" + seasonId;
            jedis.rpush(skierSeasonKey + ":day:" + dayId, liftId);

            String skierResortKey = "skier:" + skierId + ":resort:" + resortId;
            String seasonField = "season:" + seasonId;
            jedis.hincrBy(skierResortKey, seasonField, vertical);
        } catch (Exception e) {
            System.err.println("Error storing lift ride: " + e.getMessage());
            e.printStackTrace();
        }
    }
}