package cc.sportsdb.common.data.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.stream.Collectors;

import static cc.sportsdb.common.data.redis.RedisConstant.EMPTY_KEY_FORMAT;

@Configuration
@EnableCaching
@ConditionalOnClass({JedisConnection.class, RedisOperations.class, Jedis.class})
public class RedisConfig extends CachingConfigurerSupport {

    @Bean(name = RedisConstant.REDIS_TEMPLATE_NAME)
    public RedisTemplate<String, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new GenericToStringSerializer(Object.class));
        redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return redisTemplate;
    }

    @Bean
    public AutoRefreshAspect autoRefreshAspect(CacheOperationSource cacheOperationSource) {
        AutoRefreshAspect autoRefreshAspect = new AutoRefreshAspect();
        autoRefreshAspect.setCacheOperationSources(cacheOperationSource);
        return autoRefreshAspect;
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
        return (target, method, parameters) -> String.format(EMPTY_KEY_FORMAT,
                target.getClass().getName(),
                method.getName(),
                Arrays.stream(parameters)
                        .map(parameter -> parameter.getClass().getSimpleName())
                        .collect(Collectors.joining(","))
        );
    }
}
