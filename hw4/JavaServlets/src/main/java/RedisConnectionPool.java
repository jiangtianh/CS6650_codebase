import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnectionPool {
    public static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static final int POOL_SIZE = 65;

    private static JedisPool jedisPool = null;


    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(POOL_SIZE);
        poolConfig.setMinIdle(POOL_SIZE);
        poolConfig.setMaxWaitMillis(2000);

        jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT);
    }

    public static Jedis getJedis() {
        try {
            return jedisPool.getResource();
        } catch (Exception e) {
            System.out.println("Failed to get Redis connection");
            e.printStackTrace();
            return null;
        }
    }

    public static void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
