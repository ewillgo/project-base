package cc.sportsdb.common.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EmptyStackException;
import java.util.Stack;

public final class DynamicDataSourceHolder {
    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceHolder.class);
    private static final ThreadLocal<Stack<String>> HOLDER = ThreadLocal.withInitial(Stack<String>::new);

    private DynamicDataSourceHolder() {
    }

    public static String peekCurrentDataSourceName() {
        String dataSourceName = null;
        try {
            dataSourceName = HOLDER.get().peek();
            logger.info("Current data source: {}, stack: {}", dataSourceName, HOLDER.get().toString());
        } catch (EmptyStackException ignore) {
        }
        return dataSourceName;
    }

    public static String removeCurrentDataSourceName() {
        String dataSourceName = HOLDER.get().pop();
        logger.info("Removed data source: {}, stack: {}", dataSourceName, HOLDER.get().toString());
        return dataSourceName;
    }

    public static void setCurrentDataSourceName(String dataSourceName) {
        HOLDER.get().push(dataSourceName);
        logger.info("Changed to data source: {}, stack: {}", dataSourceName, HOLDER.get().toString());
    }
}
