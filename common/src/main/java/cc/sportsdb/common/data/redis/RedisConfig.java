package cc.sportsdb.common.data.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.annotation.Before;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

    @Bean(name = RedisConstant.REDIS_TEMPLATE_NAME)
    public RedisTemplate<String, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return redisTemplate;
    }

    @Bean
    public AutoRefreshAspect autoRefreshAspect() {
        return new AutoRefreshAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager(RedisTemplate<String, Object> redisTemplate) {
        RedisCacheManager redisCacheManager = new AutoRefreshRedisCacheManager(redisTemplate);
        redisCacheManager.setUsePrefix(true);
        redisCacheManager.setLoadRemoteCachesOnStartup(false);
        redisCacheManager.setDefaultExpiration(RedisConstant.DEFAULT_CACHE_EXPIRE_IN_SECOND);
        return redisCacheManager;
    }

    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            return target.getClass().getSimpleName() + "." + method.getName();
        };
    }
}
