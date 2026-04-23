package com.uh.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 角色与菜单关联表 数据层
 *
 * @author XiaoZhangTongZhi
 */
public interface SysRoleMenuMapper
{
    /**
     * 根据用户ID查询已授权的菜单编码列表
     *
     * @param userId 用户ID
     * @return 菜单编码列表
     */
    List<String> selectMenuCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询已授权的菜单编码列表
     *
     * @param roleId 角色ID
     * @return 菜单编码列表
     */
    List<String> selectMenuCodesByRoleId(@Param("roleId") Long roleId);

    /**
     * 通过角色ID删除角色和菜单关联
     *
     * @param roleId 角色ID
     * @return 结果
     */
    public int deleteRoleMenuByRoleId(Long roleId);

    /**
     * 批量删除角色菜单关联信息
     *
     * @param ids 需要删除的角色ID数组
     * @return 结果
     */
    public int deleteRoleMenu(Long[] ids);

    /**
     * 批量新增角色与菜单编码的关联
     *
     * @param roleId 角色ID
     * @param menuCodes 菜单编码列表
     * @return 结果
     */
    int batchRoleMenuCodes(@Param("roleId") Long roleId,
                           @Param("menuCodes") List<String> menuCodes);
}
