package cc.sportsdb.common.data.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisLock {

    private static Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private static final String NX = "NX";
    private static final String EX = "EX";
    private static final String OK = "OK";
    private static final String LUA_UNLOCK_SCRIPT;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        LUA_UNLOCK_SCRIPT = sb.toString();
    }

    private String lockKey;
    private String lockValue;
    private volatile boolean locked = false;
    private long expireTime = TimeUnit.SECONDS.toSeconds(1);
    private RedisTemplate<String, Object> redisTemplate;
    private final Random random = new Random();

    public RedisLock(RedisTemplate<String, Object> redisTemplate, String lockKey) {
        this(redisTemplate, lockKey, null);
    }

    public RedisLock(RedisTemplate<String, Object> redisTemplate, String lockKey, Long expireTime) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey + "__LOCk__";
        this.expireTime = expireTime == null ? this.expireTime : expireTime;
    }

    public boolean tryLock(long timeoutInSeconds) {
        lockValue = UUID.randomUUID().toString();
        long retryTimeout = TimeUnit.SECONDS.toNanos(timeoutInSeconds);
        long nowTime = System.nanoTime();

        logger.info("Starting to try to receive a [{}] lock from redis...", lockKey);
        while ((System.nanoTime() - nowTime) < retryTimeout) {
            logger.info("Trying...");
            if (OK.equalsIgnoreCase(set(lockKey, lockValue, expireTime))) {
                logger.info("Received a [{}] lock.", lockKey);
                locked = true;
                return true;
            }
            logger.info("Waiting retry until timeout...");
            sleep(10, 50000);
        }

        logger.info("Oops! Try to receive a [{}] lock timeout.", lockKey);
        return false;
    }

    public boolean lock() {
        lockValue = UUID.randomUUID().toString();
        logger.info("Starting to try to receive a [{}] lock from redis...", lockKey);
        if (OK.equalsIgnoreCase(set(lockKey, lockValue, expireTime))) {
            logger.info("Received a [{}] lock.", lockKey);
            locked = true;
            return true;
        } else {
            logger.info("A [{}] lock has been received from others.", lockKey);
            return false;
        }
    }

    public boolean lockBlock() {
        lockValue = UUID.randomUUID().toString();
        logger.info("Starting to try to receive a [{}] lock from redis...", lockKey);
        while (true) {
            logger.info("Trying...");
            if (OK.equalsIgnoreCase(set(lockKey, lockValue, expireTime))) {
                logger.info("Received a [{}] lock.", lockKey);
                locked = true;
                return true;
            }
            logger.info("Waiting retry until received a lock...");
            sleep(10, 50000);
        }
    }

    public boolean unlock() {

        if (!locked) {
            logger.info("Could not found a lock.");
            return true;
        }

        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            Object nativeConnection = connection.getNativeConnection();
            Long result = 0L;

            List<String> keys = Arrays.asList(lockKey);
            List<String> values = Arrays.asList(lockValue);

            if (nativeConnection instanceof JedisCluster) {
                result = (Long) ((JedisCluster) nativeConnection).eval(LUA_UNLOCK_SCRIPT, keys, values);
            } else if (nativeConnection instanceof Jedis) {
                result = (Long) ((Jedis) nativeConnection).eval(LUA_UNLOCK_SCRIPT, keys, values);
            }

            // 1 represent unlock success
            locked = result == 0;
            logger.info("Unlock [{}] {}.", lockKey, locked ? "fail" : "succeed");
            return result == 1;
        });

    }

    private String set(String key, String value, long seconds) {
        Assert.isTrue(!StringUtils.isEmpty(key), "Key cannot be empty.");
        return redisTemplate.execute((RedisCallback<String>) connection -> {
            Object nativeConnection = connection.getNativeConnection();
            String result = null;

            if (nativeConnection instanceof JedisCluster) {
                result = ((JedisCluster) nativeConnection).set(key, value, NX, EX, seconds);
            }

            if (nativeConnection instanceof Jedis) {
                result = ((Jedis) nativeConnection).set(key, value, NX, EX, seconds);
            }

            return result;
        });
    }

    private void sleep(long millis, int nanos) {
        try {
            Thread.sleep(millis, random.nextInt(nanos));
        } catch (InterruptedException e) {
            logger.error("Sleep interrupted.", e);
        }
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
}
