package cc.sportsdb.common.data.redis;

import org.springframework.util.MethodInvoker;

import java.util.concurrent.ConcurrentHashMap;

class MethodInvokerHolder {
    private static final ConcurrentHashMap<String, MethodInvoker> METHOD_INVOKER_MAP = new ConcurrentHashMap<>();

    public static MethodInvoker getMethodInvoker(String cacheKey) {
        return METHOD_INVOKER_MAP.get(cacheKey);
    }

    public static void setMethodInvoker(String cacheKey, MethodInvoker methodInvoker) {
        METHOD_INVOKER_MAP.putIfAbsent(cacheKey, methodInvoker);
    }
}
