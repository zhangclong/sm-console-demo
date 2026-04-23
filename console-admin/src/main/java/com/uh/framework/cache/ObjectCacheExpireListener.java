package com.uh.framework.cache;

/**
 * 缓存对象过期监听器
 * 当缓存对象过期时，会调用此监听器
 */
public interface ObjectCacheExpireListener {

    /**
     * 缓存对象过期时调用
     * @param key
     * @param value
     */
    void expiringKey(String key, Object value);

}
