package com.uh.console.service;

import com.uh.framework.cache.ObjectCache;
import com.uh.framework.cache.ObjectCacheExpireListener;
import com.uh.framework.cache.impl.LocalObjectCache;
import com.uh.system.domain.SysUser;
import com.uh.common.core.domain.LoginUser;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LocalObjectCache 的集成测试。
 * 使用 Spring Framework 原生测试支持（spring-test），不依赖 Spring Boot。
 * 通过 @ContextConfiguration 加载最小化 Spring 上下文，只包含被测 Bean 及其依赖。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ObjectCacheTester.TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ObjectCacheTester {

    /**
     * 测试专用 Spring 配置类，只注册 LocalObjectCache 及其依赖的 ScheduledExecutorService。
     * 不启动 Web 层、数据库等无关组件。
     */
    @Configuration
    static class TestConfig {
        @Bean(name = "scheduledExecutorService")
        public ScheduledExecutorService scheduledExecutorService() {
            return Executors.newScheduledThreadPool(2);
        }

        @Bean
        public LocalObjectCache localObjectCache() {
            return new LocalObjectCache();
        }
    }

    @Autowired
    private ObjectCache objectCache;

    // 过期监听器的 key，用于测试监听器是否被调用
    private final Set<String> listenerKeys = new HashSet<>();

    @BeforeAll
    void init() {
        objectCache.addExpireListener(new ExpireListener());
    }

    @AfterEach
    void cleanup() {
        // 清理每个测试写入的 key，避免测试间相互干扰
        Collection<String> allKeys = new ArrayList<>(objectCache.keys("*"));
        objectCache.deleteObject(allKeys);
    }

    @Test
    @Order(1)
    void testCacheStoreAndPatternMatch() throws IOException {
        SysUser user = new SysUser();
        user.setUserName("zhao6");
        user.setUserId(7L);
        user.setCreateTime(new Date(20000L));

        Set<String> permissions = new HashSet<>();
        permissions.add("a");
        permissions.add("b");
        permissions.add("c");

        LoginUser loginUser = new LoginUser(user, permissions);

        objectCache.setCacheObject("key1", loginUser);
        objectCache.setCacheObject("key2", loginUser, 30, TimeUnit.SECONDS);
        objectCache.setCacheObject("key3", loginUser);
        objectCache.setCacheObject("1key1", loginUser);
        objectCache.setCacheObject("2key2", loginUser);
        objectCache.setCacheObject("1key", loginUser);
        objectCache.setCacheObject("2key", loginUser);
        objectCache.setCacheObject("2dddd", loginUser);
        objectCache.setCacheObject("add_cc", loginUser);

        assertEquals(7, objectCache.keys("*key*").size());
        assertEquals(2, objectCache.keys("*key").size());
        assertEquals(3, objectCache.keys("key*").size());

        List<String> deletingKeyList = Arrays.asList(
                "key1", "key2", "key3", "1key1", "2key2", "1key", "2key", "2dddd", "add_cc");
        objectCache.deleteObject(deletingKeyList);
        assertEquals(0, objectCache.keys("*key*").size());
    }

    @Test
    @Order(2)
    void testCacheExpireAndListener() throws Exception {
        // 至少有一个监听器（在 @BeforeAll 中手动加入的 ExpireListener）
        assertTrue(objectCache.getExpireListeners().size() >= 1);

        objectCache.setCacheObject("name", "XiaoZhangTongZhi", 3, TimeUnit.SECONDS);
        assertEquals("XiaoZhangTongZhi", objectCache.getCacheObject("name"), "设置后应立即可读");

        Thread.sleep(1000);
        assertEquals("XiaoZhangTongZhi", objectCache.getCacheObject("name"), "1秒内应仍存在");

        Thread.sleep(5000); // 等待 key 过期（3秒超时 + 最多10秒扫描间隔）
        assertNull(objectCache.getCacheObject("name"), "过期后 getCacheObject 应返回 null");

        // LocalObjectCache 的过期扫描间隔为 10 秒，额外等待确保监听器被触发
        Thread.sleep(6000);
        assertTrue(listenerKeys.contains("name"), "过期监听器应在 key 过期后被触发");
    }

    class ExpireListener implements ObjectCacheExpireListener {
        @Override
        public void expiringKey(String key, Object value) {
            System.out.println("Key:" + key + " expired.");
            listenerKeys.add(key);
        }
    }
}

