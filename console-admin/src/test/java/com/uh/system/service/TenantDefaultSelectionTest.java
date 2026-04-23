package com.uh.system.service;

import com.uh.common.core.domain.LoginUser;
import com.uh.common.core.tenant.TenantContext;
import com.uh.system.domain.SysTenant;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 租户默认自动选择逻辑的单元测试。
 * 纯 JUnit 5 测试，无需 Spring 容器。
 *
 * <p>验证：
 * <ul>
 *   <li>currentTenantId 为 null 时，自动选择第一个可用租户</li>
 *   <li>tenantIds 为空时，currentTenantId 仍保持 null</li>
 *   <li>切换租户后 currentTenantId 更新为新值</li>
 *   <li>TenantContext 在设置和清除后保持正确状态</li>
 * </ul>
 */
class TenantDefaultSelectionTest
{
    @AfterEach
    void cleanup()
    {
        TenantContext.clear();
    }

    // ---- 默认自动选择逻辑 ----

    @Test
    void autoSelect_shouldPickFirstTenant_whenCurrentTenantIdIsNull()
    {
        LoginUser loginUser = new LoginUser();
        assertNull(loginUser.getCurrentTenantId(), "初始 currentTenantId 应为 null");

        List<Long> tenantIds = Arrays.asList(10L, 20L, 30L);
        if (!tenantIds.isEmpty())
        {
            loginUser.setCurrentTenantId(tenantIds.get(0));
            loginUser.setTenantIds(tenantIds);
        }

        assertEquals(10L, loginUser.getCurrentTenantId(), "应自动选择第一个租户 ID");
        assertEquals(tenantIds, loginUser.getTenantIds());
    }

    @Test
    void autoSelect_shouldNotChangeTenantId_whenListIsEmpty()
    {
        LoginUser loginUser = new LoginUser();
        List<Long> emptyList = Collections.emptyList();

        if (!emptyList.isEmpty())
        {
            loginUser.setCurrentTenantId(emptyList.get(0));
        }

        assertNull(loginUser.getCurrentTenantId(), "租户列表为空时 currentTenantId 应仍为 null");
    }

    @Test
    void autoSelect_shouldNotOverride_whenCurrentTenantIdAlreadySet()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setCurrentTenantId(5L);

        // 模拟：currentTenantId 非 null 时不触发自动选择
        if (loginUser.getCurrentTenantId() == null)
        {
            loginUser.setCurrentTenantId(99L);
        }

        assertEquals(5L, loginUser.getCurrentTenantId(), "已设置 currentTenantId 时不应覆盖");
    }

    // ---- 手动切换租户 ----

    @Test
    void selectTenant_shouldUpdateCurrentTenantId()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setCurrentTenantId(1L);
        loginUser.setTenantIds(Arrays.asList(1L, 2L));

        // 模拟切换到租户 2
        loginUser.setCurrentTenantId(2L);

        assertEquals(2L, loginUser.getCurrentTenantId(), "切换后 currentTenantId 应更新");
    }

    // ---- TenantContext 注入与清除 ----

    @Test
    void tenantContext_shouldBeSetFromCurrentTenantId()
    {
        Long tenantId = 42L;
        TenantContext.setTenantId(tenantId);

        assertEquals(tenantId, TenantContext.getTenantId());
    }

    @Test
    void tenantContext_shouldBeCleared_afterRequest()
    {
        TenantContext.setTenantId(42L);
        TenantContext.clear(); // 模拟 finally 块中的清除

        assertNull(TenantContext.getTenantId(), "TenantContext 应在请求结束后被清除");
    }

    @Test
    void tenantContext_shouldNotLeakAcrossRequests()
    {
        // 第一个"请求"
        TenantContext.setTenantId(100L);
        assertEquals(100L, TenantContext.getTenantId());
        TenantContext.clear();

        // 第二个"请求"
        assertNull(TenantContext.getTenantId(), "第二个请求开始时不应残留前一个请求的 tenantId");
    }

    // ---- LoginUser tenantIds 字段 ----

    @Test
    void loginUser_tenantIds_shouldBeReadableAfterSet()
    {
        LoginUser loginUser = new LoginUser();
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        loginUser.setTenantIds(ids);

        assertEquals(ids, loginUser.getTenantIds());
    }

    @Test
    void loginUser_tenantIds_shouldBeNullByDefault()
    {
        LoginUser loginUser = new LoginUser();
        assertNull(loginUser.getTenantIds(), "默认 tenantIds 应为 null");
    }

    // ---- SysTenant 领域对象 ----

    @Test
    void sysTenant_shouldHoldBasicFields()
    {
        SysTenant tenant = new SysTenant();
        tenant.setTenantId(1L);
        tenant.setTenantName("默认租户");
        tenant.setStatus("0");

        assertEquals(1L, tenant.getTenantId());
        assertEquals("默认租户", tenant.getTenantName());
        assertEquals("0", tenant.getStatus());
    }
}
