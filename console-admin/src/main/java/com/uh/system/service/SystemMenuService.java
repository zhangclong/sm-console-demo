package com.uh.system.service;

import java.util.List;
import java.util.Set;
import com.uh.common.core.domain.TreeSelect;
import com.uh.system.domain.MenuDefinition;
import com.uh.system.domain.vo.RouterVo;

/**
 * 系统菜单只读业务层（数据来源为 MenuRegistry）
 *
 * @author XiaoZhangTongZhi
 */
public interface SystemMenuService
{
    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public Set<String> selectMenuPermsByUserId(Long userId);

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    public Set<String> selectMenuPermsByRoleId(Long roleId);

    /**
     * 根据用户ID查询菜单树信息（MenuDefinition 版本）
     *
     * @param userId 用户ID
     * @return 菜单定义列表（仅 M/C 类型）
     */
    public List<MenuDefinition> selectMenuDefTreeByUserId(Long userId);

    /**
     * 根据角色ID查询已授权的菜单编码列表
     *
     * @param roleId 角色ID
     * @return 菜单编码列表
     */
    public List<String> selectMenuCodesByRoleId(Long roleId);

    /**
     * 构建前端路由所需要的菜单
     *
     * @param menus 菜单定义列表
     * @return 路由列表
     */
    public List<RouterVo> buildMenus(List<MenuDefinition> menus);

    /**
     * 构建前端所需要下拉树结构（MenuDefinition 版本）
     *
     * @return 下拉树结构列表
     */
    public List<TreeSelect> buildMenuTreeSelect();
}

