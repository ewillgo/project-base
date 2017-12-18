package cc.sportsdb.common.data.redis;

import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisOperations;

public class AutoRefreshRedisCache extends RedisCache {

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
    public Cache.ValueWrapper get(Object key) {
        Cache.ValueWrapper valueWrapper = super.get(key);

        return valueWrapper;
    }
}
