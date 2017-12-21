package cc.sportsdb.common.data.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

public class AutoRefreshRedisCache extends RedisCache {

    private long refreshThreshold;
    private RedisTemplate<String, Object> redisTemplate;
    private static final long LOCK_TIMEOUT = TimeUnit.SECONDS.toMillis(60);
    private static final Logger logger = LoggerFactory.getLogger(AutoRefreshRedisCache.class);

    @SuppressWarnings("unchecked")
    public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration) {
        super(name, prefix, redisOperations, expiration);
        this.redisTemplate = (RedisTemplate<String, Object>) redisOperations;
    }

    @SuppressWarnings("unchecked")
    public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration, long refreshThreshold) {
        super(name, prefix, redisOperations, expiration);
        this.refreshThreshold = refreshThreshold;
        this.redisTemplate = (RedisTemplate<String, Object>) redisOperations;
    }

    @SuppressWarnings("unchecked")
    public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration, boolean allowNullValues) {
        super(name, prefix, redisOperations, expiration, allowNullValues);
        this.redisTemplate = (RedisTemplate<String, Object>) redisOperations;
    }

    @SuppressWarnings("unchecked")
    public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration, long refreshThreshold, boolean allowNullValues) {
        super(name, prefix, redisOperations, expiration, allowNullValues);
        this.refreshThreshold = refreshThreshold;
        this.redisTemplate = (RedisTemplate<String, Object>) redisOperations;
    }

    @Override
    public Cache.ValueWrapper get(Object key) {
        Cache.ValueWrapper valueWrapper = super.get(key);
        if (valueWrapper != null) {
            refreshCache(super.getName(), String.valueOf(key));
        }
        return valueWrapper;
    }

    public void refreshCache(String cacheName, String cacheKey) {
        Long remainTTL = redisTemplate.getExpire(cacheKey);

        if (null != remainTTL && remainTTL > refreshThreshold) {
            return;
        }

        RedisLock redisLock = new RedisLock(redisTemplate,
                String.format("%s:%s", cacheName, cacheKey), LOCK_TIMEOUT);

        if (redisLock.lock()) {
            try {
                Long innerRemainTTL = redisTemplate.getExpire(cacheKey);
                if (null != innerRemainTTL && innerRemainTTL <= refreshThreshold) {

                }
            } finally {
                redisLock.unlock();
            }
        }
    }
}
