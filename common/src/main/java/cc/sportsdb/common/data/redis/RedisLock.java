package cc.sportsdb.common.data.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisLock {

    private static Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private static final String NX = "NX";
    private static final String EX = "EX";
    private static final String OK = "OK";
    private static final long DEFAULT_RETRY_TIMEOUT_MS = 100;
    private static final int DEFAULT_EXPIRE_MS = 60;
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
    private String lockKeyLog = "";
    private volatile boolean locked = false;
    private long timeout = DEFAULT_RETRY_TIMEOUT_MS;
    private int expireTime = DEFAULT_EXPIRE_MS;
    private StringRedisTemplate redisTemplate;
    private final Random random = new Random();

    public RedisLock(StringRedisTemplate redisTemplate, String lockKey) {
        this(redisTemplate, lockKey, null, null);
    }

    public RedisLock(StringRedisTemplate redisTemplate, String lockKey, int expireTime) {
        this(redisTemplate, lockKey, expireTime, null);
    }

    public RedisLock(StringRedisTemplate redisTemplate, String lockKey, long timeout) {
        this(redisTemplate, lockKey, null, timeout);
    }

    public RedisLock(StringRedisTemplate redisTemplate, String lockKey, Integer expireTime, Long timeout) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey + "_lock";
        this.expireTime = expireTime == null ? DEFAULT_EXPIRE_MS : expireTime;
        this.timeout = timeout == null ? DEFAULT_RETRY_TIMEOUT_MS : timeout;
    }

    public boolean tryLock() {
        lockValue = UUID.randomUUID().toString();
        long retryTimeout = TimeUnit.MILLISECONDS.toNanos(timeout);
        long nowTime = System.nanoTime();

        while ((System.nanoTime() - nowTime) < retryTimeout) {
            if (OK.equalsIgnoreCase(set(lockKey, lockValue, expireTime))) {
                return (locked = true);
            }
            sleep(10, 50000);
        }

        return locked;
    }

    public boolean lock() {
        lockValue = UUID.randomUUID().toString();
        String result = set(lockKey, lockValue, expireTime);
        return OK.equalsIgnoreCase(result);
    }

    public boolean lockBlock() {
        lockValue = UUID.randomUUID().toString();
        while (true) {
            String result = set(lockKey, lockValue, expireTime);
            if (OK.equalsIgnoreCase(result)) {
                return true;
            }
            sleep(10, 50000);
        }
    }

    public Boolean unlock() {

        if (!locked) {
            return true;
        }

        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            Object nativeConnection = connection.getNativeConnection();
            Long result = 0L;

            List<String> keys = new ArrayList<>(1);
            keys.add(lockKey);
            List<String> values = new ArrayList<>(1);
            values.add(lockValue);

            if (nativeConnection instanceof JedisCluster) {
                result = (Long) ((JedisCluster) nativeConnection).eval(LUA_UNLOCK_SCRIPT, keys, values);
            }

            if (nativeConnection instanceof Jedis) {
                result = (Long) ((Jedis) nativeConnection).eval(LUA_UNLOCK_SCRIPT, keys, values);
            }

            if (result == 0 && !StringUtils.isEmpty(lockKeyLog)) {
                logger.warn("Unlock {} fail.", lockKeyLog);
            }

            locked = result == 0;
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

            if (!StringUtils.isEmpty(lockKeyLog) && !StringUtils.isEmpty(result)) {
                logger.info("Fetch lock {}.", lockKeyLog);
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

    public String getLockKeyLog() {
        return lockKeyLog;
    }

    public void setLockKeyLog(String lockKeyLog) {
        this.lockKeyLog = lockKeyLog;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
