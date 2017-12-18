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

public class AutoRefreshRedisCacheManager extends RedisCacheManager {

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
            long preloadTime = pair.getSecond();
            return new AutoRefreshRedisCache(
                    cacheName, (isUsePrefix() ? getCachePrefix().prefix(cacheName) : null),
                    getRedisOperations(), expirationTime, preloadTime, cacheNullValues);
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

        TIME_MAP.put(cacheName, Pair.make(getExpirationTime(cacheName, cacheParams), getPreloadTime(cacheParams)));
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

    private long getPreloadTime(String[] cacheParams) {
        long preloadTime = 0;

        if (cacheParams.length > 2) {
            String preloadStr = cacheParams[2];
            if (!StringUtils.isEmpty(preloadStr)) {
                if (preloadStr.contains(MARK)) {
                    preloadStr = beanFactory.resolveEmbeddedValue(preloadStr);
                }
                preloadTime = Long.parseLong(preloadStr);
            }
        }

        return preloadTime;
    }

}
