package cc.sportsdb.common.data.redis;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.Ordered;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

import static cc.sportsdb.common.data.redis.RedisConstant.KEY_FORMAT;
import static cc.sportsdb.common.data.redis.RedisConstant.METHOD_KEY_FORMAT;

@Aspect
public class AutoRefreshAspect implements Ordered {


    @Pointcut("@annotation(org.springframework.cache.annotation.Cacheable)")
    public void pointcut() {
    }

    @Around("@annotation(cacheable)")
    public Object proceed(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {

        String cacheName = cacheable.value()[0];
        String cacheKey = cacheable.key();

        if (StringUtils.isEmpty(cacheName)) {
            return joinPoint.proceed();
        }

        String key = String.format(METHOD_KEY_FORMAT, cacheName,
                StringUtils.isEmpty(cacheKey)
                        ? generateKey(joinPoint.getTarget().getClass(), joinPoint.getSignature(), joinPoint.getArgs())
                        : cacheKey);

        // If cached MethodInvoker, then return.
        if (CacheInvokerHolder.getCacheMethodInvoker(key) != null) {
            return joinPoint.proceed();
        }

        MethodInvoker methodInvoker = new MethodInvoker();
        methodInvoker.setTargetObject(joinPoint.getTarget());
        methodInvoker.setTargetMethod(joinPoint.getSignature().getName());
        methodInvoker.setArguments(joinPoint.getArgs());
        methodInvoker.prepare();
        CacheInvokerHolder.setCacheMethodInvoker(key, methodInvoker);
        return joinPoint.proceed();
    }

    private String generateKey(Class<?> target, Signature signature, Object[] args) {
        return String.format(KEY_FORMAT,
                target.getName(),
                signature.getName(),
                Arrays.stream(args)
                        .map(parameter -> parameter.getClass().getSimpleName())
                        .collect(Collectors.joining(","))
        );
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
