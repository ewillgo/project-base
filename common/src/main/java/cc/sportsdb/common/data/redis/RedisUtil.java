package cc.sportsdb.common.data.redis;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

import static cc.sportsdb.common.spring.ApplicationContextHolder.getApplicationContext;

public abstract class RedisUtil {

    private static RedisTemplate redisTemplate =
            getApplicationContext().getBean(RedisConstant.REDIS_TEMPLATE_NAME, RedisTemplate.class);

    private RedisUtil() {
    }

    public static <T> T getCacheValue(String key, Class<? extends T> clazz) {
        return getCacheValue(key, clazz, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getCacheValue(String key, Class<? extends T> clazz, DataProvider<? extends T> dataProvider) {
        Object object = redisTemplate.opsForValue().get(key);

        if (object != null) {
            return (T) object;
        }

        if (dataProvider != null) {
            T value = dataProvider.getData(key);
            if (value != null) {
                redisTemplate.opsForValue().set(key, value, dataProvider.expires(), TimeUnit.SECONDS);
            }
            return value;
        }

        return null;
    }
}
