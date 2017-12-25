package cc.sportsdb.common.database;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Statement;
import java.util.Properties;

@Intercepts({
//        @Signature(type = StatementHandler.class, method = "get", args = {Statement.class, ResultHandler.class})
        @Signature(type = Executor.class, method = "get", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class MyBatisInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println(invocation.toString());
        return null;
    }

    @Override
    public Object plugin(Object target) {
        System.out.println(target.toString());
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        System.out.println(properties.toString());
    }
}
