package com.uh.system.mapper;

import com.uh.system.domain.SysTenant;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租户信息 Mapper 接口
 *
 * @author Zhang Chenlong
 */
public interface SysTenantMapper
{
    /**
     * 查询所有启用租户列表
     *
     * @return 租户列表
     */
    List<SysTenant> selectTenantList();

    /**
     * 查询指定用户可访问的租户ID列表。
     * 平台管理员（角色key包含"admin"的用户）可访问所有租户。
     *
     * @param userId 用户ID
     * @return 租户ID列表
     */
    List<Long> selectTenantIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询指定用户可访问的租户列表。
     * 平台管理员（角色key包含"admin"的用户）可访问所有租户。
     *
     * @param userId 用户ID
     * @return 租户列表
     */
    List<SysTenant> selectTenantsByUserId(@Param("userId") Long userId);

    /**
     * 根据租户ID查询租户信息
     *
     * @param tenantId 租户ID
     * @return 租户信息
     */
    SysTenant selectTenantById(@Param("tenantId") Long tenantId);

    /**
     * 验证租户ID是否属于用户（或用户是平台管理员）
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 计数，大于0表示有权限
     */
    int countUserTenant(@Param("userId") Long userId, @Param("tenantId") Long tenantId);
}
