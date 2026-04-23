package com.uh.system.web;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.uh.common.core.controller.BaseController;
import com.uh.common.core.domain.AjaxResult;
import com.uh.system.service.SystemMenuService;

/**
 * 系统菜单只读接口（运行时数据源为 MenuRegistry）
 *
 * @author XiaoZhangTongZhi
 */
@RestController
@RequestMapping("/system/menu")
public class SystemMenuController extends BaseController
{
    @Autowired
    private SystemMenuService menuService;

    /**
     * 获取菜单下拉树列表（基于 MenuRegistry）
     */
    @GetMapping("/treeselect")
    public AjaxResult treeselect()
    {
        return AjaxResult.success(menuService.buildMenuTreeSelect());
    }

    /**
     * 加载对应角色菜单列表树（返回 checkedKeys 为 menuCode 字符串列表）
     */
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    public AjaxResult roleMenuTreeselect(@PathVariable("roleId") Long roleId)
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", menuService.selectMenuCodesByRoleId(roleId));
        ajax.put("menus", menuService.buildMenuTreeSelect());
        return ajax;
    }
}

