package cc.sportsdb.common.database;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;

@Aspect
public class DataSourceSwitchAspect implements Ordered {

    @Pointcut("@annotation(cc.sportsdb.common.database.DataSource)")
    public void pointcut() {
    }

    @Around("@annotation(dataSource)")
    public Object proceed(ProceedingJoinPoint joinPoint, DataSource dataSource) throws Throwable {
        try {
            DynamicDataSourceHolder.setCurrentDataSourceName(dataSource.value());
            return joinPoint.proceed();
        } finally {
            DynamicDataSourceHolder.removeCurrentDataSourceName();
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
