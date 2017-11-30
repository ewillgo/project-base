package cc.sportsdb.common.data.redis;

import java.util.concurrent.TimeUnit;

public interface RedisConstant {
    String REDIS_TEMPLATE_NAME = "redisTemplate";
    long DEFAULT_CACHE_EXPIRE_IN_SECOND = TimeUnit.HOURS.toSeconds(12);
}
