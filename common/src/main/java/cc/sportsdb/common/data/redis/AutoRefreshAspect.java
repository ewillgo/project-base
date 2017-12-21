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

@Aspect
public class AutoRefreshAspect implements Ordered {


    @Pointcut("@annotation(org.springframework.cache.annotation.Cacheable)")
    public void pointcut() {
    }

    private static final String KEY_FORMAT = "%s__%s__";

    @Around("@annotation(cacheable)")
    public Object proceed(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {

        String cacheName = cacheable.value()[0];
        String cacheKey = cacheable.key();

        if (StringUtils.isEmpty(cacheName)) {
            return joinPoint.proceed();
        }

        String key = String.format(KEY_FORMAT, cacheName,
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
        StringBuilder sb = new StringBuilder();
        sb.append(target.getName()).append(".")
                .append(signature.getName()).append(" (")
                .append(Arrays.stream(args)
                        .map(parameter -> parameter.getClass().getSimpleName())
                        .collect(Collectors.joining(",")))
                .append(")");
        return sb.toString();
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
