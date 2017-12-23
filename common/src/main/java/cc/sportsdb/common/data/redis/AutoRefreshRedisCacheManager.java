package cc.sportsdb.common.data.redis;

import cc.sportsdb.common.helper.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

class AutoRefreshRedisCacheManager extends RedisCacheManager {

    private boolean cacheNullValues;
    private final ConcurrentHashMap<String, Pair<Long, Long>> TIME_MAP = new ConcurrentHashMap<>();

    private static final String MARK = "$";
    private static final String SEPARATOR = "#";
    private static final Logger logger = LoggerFactory.getLogger(AutoRefreshRedisCacheManager.class);

    @Autowired
    DefaultListableBeanFactory beanFactory;

    public AutoRefreshRedisCacheManager(RedisOperations redisOperations) {
        super(redisOperations);
    }

    public AutoRefreshRedisCacheManager(RedisOperations redisOperations, Collection<String> cacheNames) {
        super(redisOperations, cacheNames);
    }

    public AutoRefreshRedisCacheManager(RedisOperations redisOperations, Collection<String> cacheNames, boolean cacheNullValues) {
        super(redisOperations, cacheNames, cacheNullValues);
        this.cacheNullValues = cacheNullValues;
    }

    @Override
    protected RedisCache createCache(String cacheName) {
        Pair<Long, Long> pair = TIME_MAP.get(cacheName);
        if (pair != null) {
            long expirationTime = pair.getFirst();
            long refreshThreshold = pair.getSecond();
            return new AutoRefreshRedisCache(
                    cacheName, (isUsePrefix() ? getCachePrefix().prefix(cacheName) : null),
                    getRedisOperations(), expirationTime, refreshThreshold, cacheNullValues);
        } else {
            long expirationTime = computeExpiration(cacheName);
            return new AutoRefreshRedisCache(
                    cacheName, (isUsePrefix() ? getCachePrefix().prefix(cacheName) : null),
                    getRedisOperations(), expirationTime, 0, cacheNullValues);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Cache getCache(String name) {

        String[] cacheParams = name.split(SEPARATOR);
        String cacheName = cacheParams[0];

        if (StringUtils.isEmpty(cacheName)) {
            return null;
        }

        TIME_MAP.put(cacheName, Pair.make(
                getExpirationTime(cacheName, cacheParams), getRefreshThreshold(cacheParams)));

        return super.getCache(cacheName);
    }

    private long getExpirationTime(String cacheName, String[] cacheParams) {
        long expirationTime = computeExpiration(cacheName);

        if (cacheParams.length > 1) {
            String expirationStr = cacheParams[1];
            if (!StringUtils.isEmpty(expirationStr)) {
                if (expirationStr.contains(MARK)) {
                    expirationStr = beanFactory.resolveEmbeddedValue(expirationStr);
                }
                expirationTime = Long.parseLong(expirationStr);
            }
        }

        return expirationTime;
    }

    private long getRefreshThreshold(String[] cacheParams) {
        long refreshThreshold = 0;

        if (cacheParams.length > 2) {
            String refreshThresholdStr = cacheParams[2];
            if (!StringUtils.isEmpty(refreshThresholdStr)) {
                if (refreshThresholdStr.contains(MARK)) {
                    refreshThresholdStr = beanFactory.resolveEmbeddedValue(refreshThresholdStr);
                }
                refreshThreshold = Long.parseLong(refreshThresholdStr);
            }
        }

        return refreshThreshold;
    }

}
