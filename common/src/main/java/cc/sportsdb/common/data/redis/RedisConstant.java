package cc.sportsdb.common.data.redis;

import java.util.concurrent.TimeUnit;

interface RedisConstant {
    String REDIS_TEMPLATE_NAME = "redisTemplate";
    String CACHE_KEY_FORMAT = "%s:%s";
    String EMPTY_KEY_FORMAT = "%s.%s(%s)";
    long DEFAULT_CACHE_EXPIRE_IN_SECOND = TimeUnit.HOURS.toSeconds(12);
}
