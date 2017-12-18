package cc.sportsdb.common.data.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;

public class AutoRefreshRedisCache extends RedisCache {

    private long refreshThreshold;
    private static final Logger logger = LoggerFactory.getLogger(AutoRefreshRedisCache.class);

    public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration) {
        super(name, prefix, redisOperations, expiration);
    }

    public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration, long refreshThreshold) {
        super(name, prefix, redisOperations, expiration);
        this.refreshThreshold = refreshThreshold;
    }

    public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration, boolean allowNullValues) {
        super(name, prefix, redisOperations, expiration, allowNullValues);
    }

    public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration, long refreshThreshold, boolean allowNullValues) {
        super(name, prefix, redisOperations, expiration, allowNullValues);
        this.refreshThreshold = refreshThreshold;
    }

    @Override
    public Cache.ValueWrapper get(Object key) {
        Cache.ValueWrapper valueWrapper = super.get(key);
        if (valueWrapper != null) {
//            refreshCache(super.getName());
        }
        return valueWrapper;
    }

    @Async
    public void refreshCache(String cacheKey) {
        StringRedisTemplate redisTemplate = getRedisTemplate();
        Long remainTTL = redisTemplate.getExpire(cacheKey);

        if (null != remainTTL && remainTTL <= refreshThreshold) {
            RedisLock redisLock = new RedisLock(redisTemplate, cacheKey);
            if (redisLock.lock()) {
                logger.info("Got {} lock.", cacheKey);
                try {
                    Long innerRemainTTL = redisTemplate.getExpire(cacheKey);
                    if (null != innerRemainTTL && innerRemainTTL <= refreshThreshold) {

                    }
                } finally {
                    redisLock.unlock();
                    logger.info("Released {} lock.", cacheKey);
                }
            }

        }
    }

    private StringRedisTemplate getRedisTemplate() {
        return (StringRedisTemplate) getNativeCache();
    }
}
