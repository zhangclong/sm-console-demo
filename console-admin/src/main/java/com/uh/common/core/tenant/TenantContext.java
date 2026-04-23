package com.uh.common.core.tenant;

/**
 * 基于 ThreadLocal 的租户上下文。
 *
 * 使用规则：
 * - preHandle 中调用 setTenantId()，从 LoginUser 中取 tenantId 写入
 * - afterCompletion 中调用 clear()，防止线程池复用时数据污染
 * - isPlatformAdmin() 为 true 时跳过行级租户隔离
 */
public class TenantContext {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    /** 平台超级管理员的租户 ID，拥有所有数据访问权限 */
    public static final Long PLATFORM_TENANT_ID = 1L;

    /** 设置当前线程的租户 ID */
    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    /** 获取当前线程的租户 ID，未设置时返回 null */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 判断当前是否是平台管理员（可跨租户查看所有数据）。
     * 若 tenantId 未设置，返回 false。
     */
    public static boolean isPlatformAdmin() {
        return PLATFORM_TENANT_ID.equals(TENANT_ID.get());
    }

    /**
     * 清除当前线程的租户 ID。
     * 必须在请求结束时（afterCompletion）调用，防止线程池复用时数据污染。
     */
    public static void clear() {
        TENANT_ID.remove();
    }
}
