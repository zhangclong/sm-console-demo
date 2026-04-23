package com.uh.framework.cache;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 定义缓存的通用接口
 */
public interface ObjectCache {

    /**
     * 添加过期监听器
     * @param listener
     */
    void addExpireListener(ObjectCacheExpireListener listener);

    /**
     * 获取过期监听器
     * @return
     */
    Collection<ObjectCacheExpireListener> getExpireListeners();

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    <T> void setCacheObject(String key, T value);

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  超时时间
     * @param timeUnit 时间单位
     */
    <T> void setCacheObject(String key, T value, Integer timeout, TimeUnit timeUnit);

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    <T> T getCacheObject(String key);

    /**
     * 删除单个对象
     *
     * @param key
     */
    boolean deleteObject(String key);

    /**
     * 删除集合对象
     *
     * @param keys key集合
     * @return 删除的对象数量
     */
    boolean deleteObject(Collection<String> keys);

    /**
     * Keys Command  Returns all the keys matching the glob-style pattern as Collection of String. For example
     * if you have in the keys "foo" and "foobar" the command "KEYS foo*" will return "foo foobar".
     * Glob style patterns examples:
     * h?llo will match hello hallo hhllo
     * h*llo will match hllo heeeello
     * h[ae] llo will match hello and hallo, but not hillo
     * Use \ to escape special chars if you want to match them verbatim.
     * Time complexity: O(n) (with n being the number of keys in the DB, and assuming keys and pattern of limited length)
     *
     * @param pattern glob-style pattern, if null of "" return all keys
     * @return  all the keys matching the glob-style pattern as Collection of String
     */
    Collection<String> keys(final String pattern);

    /**
     * 判断 key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    boolean hasKey(String key);

    /**
     * 获取剩余过期时间, 单位毫秒
     * @param key
     * @return
     */
    long getExpire(String key);

}
