package com.uh.system.service;

import com.uh.system.domain.SysTenant;

import java.util.List;

/**
 * 租户会话管理服务接口
 *
 * @author Zhang Chenlong
 */
public interface SysTenantService
{
    /**
     * 查询指定用户可访问的租户列表
     *
     * @param userId 用户ID
     * @return 租户列表
     */
    List<SysTenant> selectTenantsByUserId(Long userId);

    /**
     * 查询指定用户可访问的租户ID列表
     *
     * @param userId 用户ID
     * @return 租户ID列表
     */
    List<Long> selectTenantIdsByUserId(Long userId);

    /**
     * 验证租户ID是否属于用户，不属于则抛出 ServiceException
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     */
    void validateUserTenant(Long userId, Long tenantId);
}
