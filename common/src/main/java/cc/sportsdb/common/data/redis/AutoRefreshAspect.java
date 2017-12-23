package cc.sportsdb.common.data.redis;

import cc.sportsdb.common.util.ReflectionUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.core.Ordered;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static cc.sportsdb.common.data.redis.RedisConstant.CACHE_KEY_FORMAT;
import static cc.sportsdb.common.data.redis.RedisConstant.EMPTY_KEY_FORMAT;

@Aspect
class AutoRefreshAspect extends CacheAspectSupport implements Ordered {

    private static final String REFLECTION_METHOD_NAME = "generateKey";

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

        MethodInvoker methodInvoker = new MethodInvoker();
        methodInvoker.setTargetObject(joinPoint.getTarget());
        methodInvoker.setTargetMethod(joinPoint.getSignature().getName());
        methodInvoker.setArguments(joinPoint.getArgs());
        methodInvoker.prepare();

        String key = String.format(CACHE_KEY_FORMAT, cacheName.split("#")[0],
                StringUtils.isEmpty(cacheKey)
                        ? generateKey(joinPoint.getTarget().getClass(), joinPoint.getSignature(), joinPoint.getArgs())
                        : translateCacheableKey(methodInvoker));

        MethodInvokerHolder.setMethodInvoker(key, methodInvoker);
        return joinPoint.proceed();
    }

    private String translateCacheableKey(MethodInvoker methodInvoker) {
        Collection<CacheOperation> operations =
                getCacheOperationSource().getCacheOperations(methodInvoker.getPreparedMethod(), methodInvoker.getTargetClass());

        CacheOperation cacheOperation = operations.iterator().next();
        CacheOperationContext cacheOperationContext =
                getOperationContext(cacheOperation, methodInvoker.getPreparedMethod(), methodInvoker.getArguments(), methodInvoker.getTargetObject(), methodInvoker.getTargetClass());

        return ReflectionUtil.invokeMethod(
                cacheOperationContext, REFLECTION_METHOD_NAME, new Class<?>[]{Object.class}, new Object[]{new Object()}).toString();
    }

    private String generateKey(Class<?> target, Signature signature, Object[] args) {
        return String.format(EMPTY_KEY_FORMAT,
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
