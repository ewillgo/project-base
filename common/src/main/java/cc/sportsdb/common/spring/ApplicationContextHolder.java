package cc.sportsdb.common.spring;

import org.springframework.context.ApplicationContext;

public abstract class ApplicationContextHolder {

    private static volatile boolean inited = false;
    private static ApplicationContext APPLICATION_CONTEXT;

    private ApplicationContextHolder() {
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        if (!inited) {
            APPLICATION_CONTEXT = applicationContext;
            inited = true;
        }
    }

    public static ApplicationContext getApplicationContext() {
        return APPLICATION_CONTEXT;
    }
}
