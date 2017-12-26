package cc.sportsdb.common.database.mybatis;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

import static cc.sportsdb.common.database.mybatis.MyBatisConstant.METHOD_PREPARE;
import static cc.sportsdb.common.database.mybatis.MyBatisConstant.METHOD_QUERY;

@Intercepts({
        @Signature(type = StatementHandler.class, method = METHOD_QUERY, args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = METHOD_PREPARE, args = {Connection.class, Integer.class})
})
class MyBatisInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        if (METHOD_PREPARE.equals(invocation.getMethod().getName())) {

        } else if (METHOD_QUERY.equals(invocation.getMethod().getName())) {

        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
