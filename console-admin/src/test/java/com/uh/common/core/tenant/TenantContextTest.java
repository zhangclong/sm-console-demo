package com.uh.common.core.tenant;

import org.junit.jupiter.api.*;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TenantContext 单元测试。
 * 纯 JUnit 5 测试，无需 Spring 容器，可在任何环境直接运行。
 * 验证：ThreadLocal 隔离、setTenantId/getTenantId、clear 语义、isPlatformAdmin 判断。
 */
class TenantContextTest {

    @AfterEach
    void cleanup() {
        // 每个测试后清理，防止影响后续测试（ThreadLocal 残留）
        TenantContext.clear();
    }

    @Test
    void shouldReturnNull_whenTenantIdNotSet() {
        assertNull(TenantContext.getTenantId(), "未调用 setTenantId 时应返回 null");
    }

    @Test
    void shouldReturnTenantId_afterSet() {
        TenantContext.setTenantId(42L);
        assertEquals(42L, TenantContext.getTenantId());
    }

    @Test
    void shouldReturnNull_afterClear() {
        TenantContext.setTenantId(42L);
        TenantContext.clear();
        assertNull(TenantContext.getTenantId(), "clear() 后 getTenantId() 应返回 null");
    }

    @Test
    void shouldReturnTrue_whenTenantIdIsPlatformAdminId() {
        TenantContext.setTenantId(TenantContext.PLATFORM_TENANT_ID);
        assertTrue(TenantContext.isPlatformAdmin());
    }

    @Test
    void shouldReturnFalse_whenTenantIdIsNotPlatformAdminId() {
        TenantContext.setTenantId(2L);
        assertFalse(TenantContext.isPlatformAdmin());
    }

    @Test
    void shouldReturnFalse_whenTenantIdNotSet() {
        assertFalse(TenantContext.isPlatformAdmin(), "未设置 tenantId 时不应被认为是平台管理员");
    }

    @Test
    void shouldIsolateAcrossThreads() throws InterruptedException, ExecutionException {
        // 主线程设置 tenantId
        TenantContext.setTenantId(100L);

        // 新创建的子线程不应继承主线程的 ThreadLocal 值
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> futureInChildThread = executor.submit(TenantContext::getTenantId);
        Long childThreadTenantId = futureInChildThread.get();
        executor.shutdown();

        assertNull(childThreadTenantId, "子线程不应继承主线程的 tenantId（ThreadLocal 隔离）");
        assertEquals(100L, TenantContext.getTenantId(), "主线程的 tenantId 不应受子线程影响");
    }

    @Test
    void shouldUpdateTenantId_whenSetMultipleTimes() {
        TenantContext.setTenantId(1L);
        TenantContext.setTenantId(2L);
        assertEquals(2L, TenantContext.getTenantId(), "重复 set 应覆盖之前的值");
    }

    @Test
    void clearShouldBeIdempotent() {
        // 未 set 直接 clear，不应抛异常
        assertDoesNotThrow(() -> {
            TenantContext.clear();
            TenantContext.clear();
        });
    }
}
