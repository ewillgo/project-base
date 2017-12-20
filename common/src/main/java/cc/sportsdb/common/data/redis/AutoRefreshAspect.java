package cc.sportsdb.common.data.redis;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.Ordered;

@Aspect
public class AutoRefreshAspect implements Ordered {

    @Pointcut("@annotation(org.springframework.cache.annotation.Cacheable)")
    public void pointcut() {
    }

    @Around("@annotation(cacheable)")
    public Object proceed(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        return joinPoint.proceed();
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
