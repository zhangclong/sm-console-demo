package com.uh.framework.cache.impl;

import java.util.*;
import java.util.concurrent.*;

import com.uh.framework.cache.ObjectCacheExpireListener;
import com.uh.framework.cache.ObjectCache;
import com.uh.common.utils.StringUtils;
import com.uh.common.utils.TimeoutUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;


/**
 * 本地缓存实现, 数据保存在本地内存中。
 */
@Component
public class LocalObjectCache implements ObjectCache {

    private final static long CHECK_INTERVAL = 10; //过期扫描的时间间隔单位秒

    private final Map<String, Object> data = new ConcurrentHashMap<>();

    private final Map<String, Long> keysExpireTime = new ConcurrentHashMap<String, Long>();

    private final Queue<ObjectCacheExpireListener> listeners = new ConcurrentLinkedQueue<>();

    @Resource(name = "scheduledExecutorService")
    private ScheduledExecutorService taskExecutor;

    @PostConstruct
    public void init() {
        taskExecutor.scheduleAtFixedRate(this::removeExpired, 0, CHECK_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public void addExpireListener(ObjectCacheExpireListener listener) {
        listeners.add(listener);
    }

    @Override
    public Collection<ObjectCacheExpireListener> getExpireListeners() {
        return listeners;
    }

    @Override
    public <T> void setCacheObject(String key, T value) {
        data.put(key, value);
    }

    @Override
    public <T> void setCacheObject(String key, T value, Integer timeout, TimeUnit unit) {
        //If timeout > 0 save it as expire time.
        if (timeout > 0) {
            Long expireTime = System.currentTimeMillis() + TimeoutUtils.toMillis(timeout, unit);
            keysExpireTime.put(key, expireTime);
        }

        data.put(key, value);
    }

    @Override
    public <T> T getCacheObject(String key) {
        if (!hasKey(key)) {
            return null;
        }
        return (T) data.get(key);
    }

    public boolean deleteObject(final String key) {
        if (keysExpireTime.containsKey(key)) {
            keysExpireTime.remove(key);
        }

        if (data.containsKey(key)) {
            data.remove(key);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean deleteObject(Collection<String> keys) {
        long deleted = 0;
        for (String key : keys) {
            if (deleteObject(key)) {
                deleted++;
            }
        }
        return deleted > 0;
    }


    public Collection<String> keys(final String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return data.keySet();
        }

        Set<String> keys = new HashSet<>();
        for (String key : data.keySet()) {
            if (StringUtils.matchesGlobPattern(key, pattern)) {
                keys.add(key);
            }
        }

        return keys;
    }

    @Override
    public boolean hasKey(String key) {
        if (isExpired(key))
            return false;
        return data.containsKey(key);
    }

    /**
     * 如果键不存在，redisTemplate.getExpire 方法会返回 -2，表示键不存在。
     * 如果键存在但没有设置过期时间，redisTemplate.getExpire 方法会返回 -1，表示键没有过期时间限制。
     * 如果键存在且已经过期，redisTemplate.getExpire 方法会返回 0，表示键已经过期。
     *
     * @param key
     * @return 单位秒
     */
    @Override
    public long getExpire(String key) {

        if (!data.containsKey(key)) return -2L;
        if (!keysExpireTime.containsKey(key)) return -1L;
        if (isExpired(key)) return 0L;

        return (keysExpireTime.get(key) - System.currentTimeMillis()) / 1000;

    }

    /**
     * 删除所有过期的数据，针对设置了timeout的数据。
     */
    private void removeExpired() {
        long time = System.currentTimeMillis();
        if (!keysExpireTime.isEmpty()) {
            for (Map.Entry<String, Long> expireEn : keysExpireTime.entrySet()) {
                Long expire = expireEn.getValue();
                if (time > expire) {
                    CompletableFuture.runAsync(() -> {
                        for (ObjectCacheExpireListener listener : listeners) {
                            listener.expiringKey(expireEn.getKey(), data.get(expireEn.getKey()));
                        }
                    }, taskExecutor).thenRun(() -> deleteObject(expireEn.getKey()));

                }
            }
        }
    }

    /**
     * 判断响应key是否过期 true 为过期，过期并删除并通知相关函数
     *
     * @param key
     * @return
     */
    private boolean isExpired(String key) {
        if (keysExpireTime.containsKey(key)) {
            // 存在过期时间
            Long expiredTime = keysExpireTime.get(key);
            return System.currentTimeMillis() > expiredTime;
        }
        return false;
    }


}
