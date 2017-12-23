package cc.sportsdb.common.data.redis;

import cc.sportsdb.common.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.MethodInvoker;

import java.util.concurrent.TimeUnit;

import static cc.sportsdb.common.data.redis.RedisConstant.CACHE_KEY_FORMAT;

class AutoRefreshRedisCache extends RedisCache {

    private long expiration;
    private long refreshThreshold;
    private RedisTemplate<String, Object> redisTemplate;
    private static final long LOCK_EXPIRES = TimeUnit.SECONDS.toSeconds(3);
    private static final Logger logger = LoggerFactory.getLogger(AutoRefreshRedisCache.class);

    @SuppressWarnings("unchecked")
    public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration) {
        super(name, prefix, redisOperations, expiration);
        this.expiration = expiration;
        this.redisTemplate = (RedisTemplate<String, Object>) redisOperations;
    }

    @SuppressWarnings("unchecked")
    public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration, long refreshThreshold) {
        super(name, prefix, redisOperations, expiration);
        this.expiration = expiration;
        this.refreshThreshold = refreshThreshold;
        this.redisTemplate = (RedisTemplate<String, Object>) redisOperations;
    }

    @SuppressWarnings("unchecked")
    public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration, boolean allowNullValues) {
        super(name, prefix, redisOperations, expiration, allowNullValues);
        this.expiration = expiration;
        this.redisTemplate = (RedisTemplate<String, Object>) redisOperations;
    }

    @SuppressWarnings("unchecked")
    public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration, long refreshThreshold, boolean allowNullValues) {
        super(name, prefix, redisOperations, expiration, allowNullValues);
        this.expiration = expiration;
        this.refreshThreshold = refreshThreshold;
        this.redisTemplate = (RedisTemplate<String, Object>) redisOperations;
    }

    @Override
    public Cache.ValueWrapper get(Object key) {
        Cache.ValueWrapper valueWrapper = super.get(String.valueOf(key));
        if (refreshThreshold != 0 && valueWrapper != null) {
            refreshCache(super.getName(), key.toString());
        }
        return valueWrapper;
    }

    public void refreshCache(String cacheName, String cacheKey) {
        String redisKey = String.format(CACHE_KEY_FORMAT, cacheName, cacheKey);
        Long remainTTL = redisTemplate.getExpire(redisKey);

        if (null != remainTTL && remainTTL > refreshThreshold) {
            return;
        }

        String lockKey = String.format(CACHE_KEY_FORMAT, cacheName, HashUtil.md5(cacheKey));
        RedisLock redisLock = new RedisLock(redisTemplate, lockKey, LOCK_EXPIRES);

        if (redisLock.lock()) {
            try {
                Long innerRemainTTL = redisTemplate.getExpire(redisKey);
                if (null != innerRemainTTL && innerRemainTTL <= refreshThreshold) {
                    try {
                        MethodInvoker methodInvoker = MethodInvokerHolder.getMethodInvoker(redisKey);
                        if (methodInvoker != null) {
                            redisTemplate.opsForValue().set(
                                    redisKey, methodInvoker.invoke(), expiration, TimeUnit.SECONDS);
                            logger.info("Auto invoked cache method. key: [{}]", redisKey);
                        }
                    } catch (Exception e) {
                        logger.error("Auto invoke cache method fail, key: [{}]", redisKey);
                    }
                }
            } finally {
                redisLock.unlock();
            }
        }
    }
}
