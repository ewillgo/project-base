package cc.sportsdb.common.data.redis;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public final class RedisUtil implements ApplicationContextAware {

    private static RedisTemplate redisTemplate;

    private RedisUtil() {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        redisTemplate = applicationContext.getBean(RedisConstant.REDIS_TEMPLATE_NAME, RedisTemplate.class);
    }
}
