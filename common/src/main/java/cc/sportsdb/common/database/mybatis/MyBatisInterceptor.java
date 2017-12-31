package cc.sportsdb.common.database.mybatis;

import cc.sportsdb.common.util.ReflectionUtil;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static cc.sportsdb.common.database.mybatis.MyBatisConstant.*;

@Intercepts({
        @Signature(type = StatementHandler.class, method = METHOD_QUERY, args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = METHOD_PREPARE, args = {Connection.class, Integer.class})
})
class MyBatisInterceptor implements Interceptor {

    private ThreadLocal<Page<?>> pageThreadLocal = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(MyBatisInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        if (METHOD_PREPARE.equals(invocation.getMethod().getName())) {
            prepareStatementHandler(invocation);
        } else if (METHOD_QUERY.equals(invocation.getMethod().getName())) {
            return queryHandler(invocation);
        }

        return invocation.proceed();
    }

    @SuppressWarnings("unchecked")
    private Object queryHandler(Invocation invocation) throws InvocationTargetException, IllegalAccessException {
        if (pageThreadLocal.get() == null) {
            return invocation.proceed();
        }

        Object result;
        Page<?> page = pageThreadLocal.get();

        try {
            result = invocation.proceed();
            if (result instanceof List) {
                page.setDataList((List) result);
            }
        } finally {
            pageThreadLocal.remove();
        }

        return page;
    }

    private void prepareStatementHandler(Invocation invocation) throws SQLException {
        RoutingStatementHandler handler = (RoutingStatementHandler) invocation.getTarget();

        if (!hasPageInfo(handler.getBoundSql().getParameterObject())) {
            return;
        }

        Connection connection = (Connection) invocation.getArgs()[0];
        MyBatisConstant.DBType dbType = MyBatisConstant.DBType.of(connection.getMetaData().getDatabaseProductName());
        StatementHandler delegate = ReflectionUtil.getFieldValue(handler, "delegate");
        BoundSql boundSql = delegate.getBoundSql();

        Page<?> page = new Page<>();
        fillPageInfo(boundSql.getParameterObject(), page);
        executeAndSetTotalCount(connection, delegate, boundSql, page);
        injectLimitSql(boundSql, dbType, page);
        pageThreadLocal.set(page);
    }

    private void executeAndSetTotalCount(Connection connection, StatementHandler delegate, BoundSql boundSql, Page<?> page) {
        String countSql = buildCountSql(boundSql.getSql());
        MappedStatement mappedStatement = ReflectionUtil.getFieldValue(delegate, "mappedStatement");

        BoundSql countBoundSql = new BoundSql(
                mappedStatement.getConfiguration(), countSql, boundSql.getParameterMappings(), boundSql.getParameterObject());

        ParameterHandler parameterHandler = new DefaultParameterHandler(
                mappedStatement, boundSql.getParameterObject(), countBoundSql);

        try (PreparedStatement ps = connection.prepareStatement(countSql)) {
            parameterHandler.setParameters(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    page.setTotalRecord(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Executing page's count sql fail.", e);
        }
    }

    private String buildCountSql(String sql) {
        return String.format("SELECT COUNT(*) FROM (%s) t", sql);
    }

    private void fillPageInfo(Object parameterObject, Page<?> page) {
        Map<?, ?> parameterMap = (Map<?, ?>) parameterObject;
        for (Map.Entry<?, ?> entry : parameterMap.entrySet()) {
            if (((String) entry.getKey()).equalsIgnoreCase(FIELD_PAGE_NO)) {
                page.setPageNo((Integer) entry.getValue());
            } else if (((String) entry.getKey()).equalsIgnoreCase(FIELD_PAGE_SIZE)) {
                page.setPageSize((Integer) entry.getValue());
            }
        }
    }

    private void injectLimitSql(BoundSql boundSql, MyBatisConstant.DBType dbType, Page<?> page) {
        String limitSql = buildPageSql(boundSql.getSql(), dbType, page);
        ReflectionUtil.setFieldValue(boundSql, "sql", limitSql);
    }

    private String buildPageSql(String sql, MyBatisConstant.DBType dbType, Page<?> page) {
        switch (dbType) {
            case MYSQL:
                return String.format("%s LIMIT %d, %d",
                        sql, ((page.getPageNo() - 1) * page.getPageSize()), page.getPageSize());
            case ORACLE:
                int offset = (page.getPageNo() - 1) * page.getPageSize() + 1;
                return String.format("SELECT * FROM (SELECT u.*, rownum r FROM (%s) u WHERE rownum < %d) WHERE r >= %d",
                        sql, (offset + page.getPageSize()), offset);
            default:
                return null;
        }
    }

    private boolean hasPageInfo(Object parameterObject) {
        if (!(parameterObject instanceof Map<?, ?>)) {
            return false;
        }

        boolean hasPageNo = false;
        boolean hasPageSize = false;
        Map<?, ?> parameterMap = (Map<?, ?>) parameterObject;

        for (Map.Entry<?, ?> entry : parameterMap.entrySet()) {
            if (((String) entry.getKey()).equalsIgnoreCase(FIELD_PAGE_NO)
                    && (entry.getValue() instanceof Integer)) {
                hasPageNo = true;
            } else if (((String) entry.getKey()).equalsIgnoreCase(FIELD_PAGE_SIZE)
                    && (entry.getValue() instanceof Integer)) {
                hasPageSize = true;
            }
        }

        return hasPageNo && hasPageSize;
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
