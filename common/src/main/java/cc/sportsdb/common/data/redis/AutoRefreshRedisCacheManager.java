package cc.sportsdb.common.data.redis;

import cc.sportsdb.common.spring.ApplicationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class AutoRefreshRedisCacheManager extends RedisCacheManager {

    private boolean cacheNullValues;
    private RedisCacheManager redisCacheManager;
    private static final String MARK = "$";
    private static final String SEPARATOR = "#";
    private static final Logger logger = LoggerFactory.getLogger(AutoRefreshRedisCacheManager.class);

    private static final String SUPER_FIELD_CACHEMAP = "cacheMap";
    private static final String SUPER_METHOD_UPDATECACHENAMES = "updateCacheNames";

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
        long expiration = computeExpiration(cacheName);
        return new AutoRefreshRedisCache(cacheName, (isUsePrefix() ? getCachePrefix().prefix(cacheName) : null), getRedisOperations(), expiration, cacheNullValues);
    }

    @Override
    public Cache getCache(String name) {

        String[] cacheParams = name.split(SEPARATOR);
        String cacheName = cacheParams[0];

        if (StringUtils.isEmpty(cacheName)) {
            return null;
        }

        long expirationSecondTime = getExpirationTime(cacheName, cacheParams);
        long preloadSecondTime = getPreloadTime(cacheParams);


        Object object = ReflectionUtils.findField(getInstance().getClass(), SUPER_FIELD_CACHEMAP, ConcurrentHashMap.class);
        if (object != null && object instanceof ConcurrentHashMap) {
            ConcurrentHashMap<String, Cache> cacheMap = (ConcurrentHashMap<String, Cache>) object;
            return getCache(cacheName, expirationSecondTime, preloadSecondTime, cacheMap);
        } else {
            return super.getCache(cacheName);
        }
    }

    public Cache getCache(String cacheName, long expirationTime, long preloadTime, ConcurrentHashMap<String, Cache> cacheMap) {
        Cache cache = cacheMap.get(cacheName);
        if (cache != null) {
            return cache;
        } else {
            synchronized (cacheMap) {
                cache = cacheMap.get(cacheName);
                if (cache == null) {
//                    cache = getMissingCache(cacheName, expirationTime, preloadTime);
                    if (cache != null) {
                        cache = decorateCache(cache);
                        cacheMap.put(cacheName, cache);

                        Class<?> parameterTypes = String.class;
                        Object[] parameters = {cacheName};
                        Method method = ReflectionUtils.findMethod(getInstance().getClass(), SUPER_METHOD_UPDATECACHENAMES, parameterTypes);
                        ReflectionUtils.invokeMethod(method, getInstance(), SUPER_METHOD_UPDATECACHENAMES, parameterTypes, parameters);
                    }
                }
                return cache;
            }
        }
    }

    private long getExpirationTime(String cacheName, String[] cacheParams) {
        long expirationSecondTime = computeExpiration(cacheName);

        if (cacheParams.length > 1) {
            String expirationStr = cacheParams[1];
            if (!StringUtils.isEmpty(expirationStr)) {
                if (expirationStr.contains(MARK)) {
                    expirationStr = beanFactory.resolveEmbeddedValue(expirationStr);
                }
                expirationSecondTime = Long.parseLong(expirationStr);
            }
        }

        return expirationSecondTime;
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

    private RedisCacheManager getInstance() {
        if (redisCacheManager == null) {
            redisCacheManager = ApplicationContextHolder.getApplicationContext().getBean(RedisCacheManager.class);
        }
        return redisCacheManager;
    }

    static class AutoRefreshRedisCache extends RedisCache {

        private long preload;

        public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration) {
            super(name, prefix, redisOperations, expiration);
        }

        public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration, long preload) {
            super(name, prefix, redisOperations, expiration);
            this.preload = preload;
        }

        public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration, boolean allowNullValues) {
            super(name, prefix, redisOperations, expiration, allowNullValues);
        }

        public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration, long preload, boolean allowNullValues) {
            super(name, prefix, redisOperations, expiration, allowNullValues);
            this.preload = preload;
        }

        @Override
        public ValueWrapper get(Object key) {
            ValueWrapper valueWrapper = super.get(key);

            return valueWrapper;
        }
    }
}
