package cc.sportsdb.common.data.redis;

import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisOperations;

import java.util.Collection;

public class AutoRefreshRedisCacheManager extends RedisCacheManager {

    private static final String SEPARATOR = "#";
    private boolean cacheNullValues;

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

        long expiration = 0;
        try {
            expiration = Long.valueOf(cacheParams[1]);
            setDefaultExpiration(expiration);
        } catch (Exception e) {
        }

        return super.getCache(cacheName);
    }

    static class AutoRefreshRedisCache extends RedisCache {

        public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration) {
            super(name, prefix, redisOperations, expiration);
            String sep = SEPARATOR;
        }

        public AutoRefreshRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration, boolean allowNullValues) {
            super(name, prefix, redisOperations, expiration, allowNullValues);
        }

        @Override
        public ValueWrapper get(Object key) {
            System.out.println(this.toString());
            return super.get(key);
        }
    }
}
