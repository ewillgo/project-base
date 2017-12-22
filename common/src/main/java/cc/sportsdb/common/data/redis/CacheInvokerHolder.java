package cc.sportsdb.common.data.redis;

import org.springframework.util.MethodInvoker;

import java.util.concurrent.ConcurrentHashMap;

class CacheInvokerHolder {
    private static final ConcurrentHashMap<String, MethodInvoker> CACHE_INVOKER_MAP = new ConcurrentHashMap<>();

    public static MethodInvoker getCacheMethodInvoker(String cacheKey) {
        return CACHE_INVOKER_MAP.get(cacheKey);
    }

    public static void setCacheMethodInvoker(String cacheKey, MethodInvoker methodInvoker) {
        CACHE_INVOKER_MAP.putIfAbsent(cacheKey, methodInvoker);
    }
}
