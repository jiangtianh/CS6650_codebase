import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class RabbitMQChannelPool {

    private static final int POOL_SIZE = 65;
    public static final String QUEUE_NAME = "liftRidesExchange";
    private static RabbitMQChannelPool instance;
    private BlockingQueue<Channel> channelPool;

    private RabbitMQChannelPool() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("localhost");
        factory.setHost("35.155.53.0");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");

        Connection connection = factory.newConnection();
        channelPool = new LinkedBlockingQueue<>(POOL_SIZE);

        for (int i = 0; i < POOL_SIZE; i++) {
            Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channelPool.add(channel);
        }
    }

    public static synchronized RabbitMQChannelPool getInstance() throws IOException, TimeoutException {
        if (instance == null) {
            instance = new RabbitMQChannelPool();
        }
        return instance;
    }

    public Channel borrowChannel() throws InterruptedException {
        return channelPool.take();
    }

    public void returnChannel(Channel channel) {
        channelPool.add(channel);
    }

}
