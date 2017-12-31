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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static cc.sportsdb.common.database.mybatis.MyBatisConstant.METHOD_PREPARE;
import static cc.sportsdb.common.database.mybatis.MyBatisConstant.METHOD_QUERY;

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
            return prepareStatementHandler(invocation);
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
            page.setDataList((result != null && result instanceof List) ? (List) result : new ArrayList<>());
        } finally {
            pageThreadLocal.remove();
        }

        return result;
    }

    private Object prepareStatementHandler(Invocation invocation)
            throws SQLException, InvocationTargetException, IllegalAccessException {

        RoutingStatementHandler handler = (RoutingStatementHandler) invocation.getTarget();

        if (!hasPageInfo(handler.getBoundSql().getParameterObject())) {
            return invocation.proceed();
        }

        Connection connection = (Connection) invocation.getArgs()[0];
        MyBatisConstant.DBType dbType = MyBatisConstant.DBType.of(connection.getMetaData().getDatabaseProductName());
        StatementHandler delegate = ReflectionUtil.getFieldValue(handler, "delegate");
        BoundSql boundSql = delegate.getBoundSql();

        Page<?> page = getPage(boundSql.getParameterObject());

        if (page == null) {
            return invocation.proceed();
        }

        executeAndSetTotalCount(connection, delegate, boundSql, page);
        injectLimitSql(boundSql, dbType, page);
        pageThreadLocal.set(page);
        return invocation.proceed();
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
                    page.setTotalPage((int) Math.ceil(page.getTotalRecord() * 1.0 / page.getPageSize()));
                    page.setFirstPage(page.getPageNo() == 1);
                    page.setLastPage(page.getPageNo() == page.getTotalPage());
                }
            }
        } catch (SQLException e) {
            logger.error("Executing page's count sql fail.", e);
        }
    }

    private String buildCountSql(String sql) {
        return String.format("SELECT COUNT(*) FROM (%s) t", sql);
    }

    private Page<?> getPage(Object parameterObject) {

        if (parameterObject instanceof Page) {
            return (Page<?>) parameterObject;
        }

        Map<?, ?> parameterMap = (Map<?, ?>) parameterObject;
        for (Map.Entry<?, ?> entry : parameterMap.entrySet()) {
            if (entry.getValue() instanceof Page) {
                return (Page<?>) entry.getValue();
            }
        }

        return null;
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
        return (parameterObject instanceof Map<?, ?>)
                ? ((Map<?, ?>) parameterObject).values().stream().anyMatch((value) -> value.getClass().isInstance(Page.class))
                : parameterObject instanceof Page;
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
