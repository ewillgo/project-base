package cc.sportsdb.common.data.redis;

public interface DataProvider<T> {
    long DEFAULT_CACHE_EXPIRE_IN_SECOND = RedisConstant.DEFAULT_CACHE_EXPIRE_IN_SECOND;
    T getData(String key);
    long expires();
}
