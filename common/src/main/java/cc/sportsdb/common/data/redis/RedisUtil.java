package cc.sportsdb.common.data.redis;

import cc.sportsdb.common.spring.ApplicationContextHolder;
import org.springframework.data.redis.core.RedisTemplate;

public abstract class RedisUtil {

    private static RedisTemplate redisTemplate = ApplicationContextHolder.getApplicationContext().getBean(RedisConstant.REDIS_TEMPLATE_NAME, RedisTemplate.class);

}
