package com.uh.system.web;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import com.uh.common.annotation.PrePermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.uh.common.annotation.Log;
import com.uh.common.constant.UserConstants;
import com.uh.common.core.controller.BaseController;
import com.uh.common.core.domain.AjaxResult;
import com.uh.system.domain.SysRole;
import com.uh.system.domain.SysUser;
import com.uh.common.core.domain.LoginUser;
import com.uh.common.core.page.TableDataInfo;
import com.uh.common.enums.BusinessTypeConstants;
import com.uh.common.utils.StringUtils;
import com.uh.common.utils.poi.ExcelUtil;
import com.uh.system.service.SysPermissionService;
import com.uh.system.service.TokenService;
import com.uh.system.domain.SysUserRole;
import com.uh.system.service.SysRoleSvc;
import com.uh.system.service.SysUserService;

/**
 * 角色信息
 *
 * @author XiaoZhangTongZhi
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController extends BaseController
{
    @Autowired
    private SysRoleSvc roleService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private SysUserService userService;

    @PrePermission("system:role:list")
    @GetMapping("/list")
    public TableDataInfo list(SysRole role)
    {
        startPage();
        List<SysRole> list = roleService.selectRoleList(role);
        return getDataTable(list);
    }

    @Log(title = "system:role", businessType = BusinessTypeConstants.EXPORT)
    @PrePermission("system:role:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysRole role)
    {
        List<SysRole> list = roleService.selectRoleList(role);
        ExcelUtil<SysRole> util = new ExcelUtil<SysRole>(SysRole.class);
        util.exportExcel(response, list, "角色数据");
    }

    /**
     * 根据角色编号获取详细信息
     */
    @PrePermission("system:role:query")
    @GetMapping(value = "/{roleId}")
    public AjaxResult getInfo(@PathVariable("roleId") Long roleId)
    {
        return AjaxResult.success(roleService.selectRoleById(roleId));
    }

    /**
     * 新增角色
     */
    @PrePermission("system:role:add")
    @Log(title = "system:role", businessType = BusinessTypeConstants.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysRole role)
    {
        if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role)))
        {
            return AjaxResult.error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        else if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleKeyUnique(role)))
        {
            return AjaxResult.error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setCreateBy(getUsername());
        return toAjax(roleService.insertRole(role));
    }

    /**
     * 修改保存角色
     */
    @PrePermission("system:role:edit")
    @Log(title = "system:role", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@Validated @RequestBody SysRole role)
    {
        roleService.checkRoleAllowed(role);
        if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role)))
        {
            return AjaxResult.error("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        else if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleKeyUnique(role)))
        {
            return AjaxResult.error("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setUpdateBy(getUsername());

        if (roleService.updateRole(role) > 0)
        {
            LoginUser loginUser = getLoginUser();
            if (StringUtils.isNotNull(loginUser.getUser()) && !loginUser.getUser().isAdmin())
            {
                loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
                loginUser.setUser(userService.selectUserByUserName(loginUser.getUser().getUserName()));
                tokenService.refreshLoginUser(loginUser);
            }
            return AjaxResult.success();
        }
        return AjaxResult.error("修改角色'" + role.getRoleName() + "'失败，请联系管理员");
    }


    /**
     * 状态修改
     */
    @PrePermission("system:role:edit")
    @Log(title = "system:role", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysRole role)
    {
        roleService.checkRoleAllowed(role);
        role.setUpdateBy(getUsername());
        return toAjax(roleService.updateRoleStatus(role));
    }

    /**
     * 删除角色
     */
    @PrePermission("system:role:remove")
    @Log(title = "system:role", businessType = BusinessTypeConstants.DELETE)
    @GetMapping("/delete/{roleIds}")
    public AjaxResult remove(@PathVariable("roleIds") Long[] roleIds)
    {
        return toAjax(roleService.deleteRoleByIds(roleIds));
    }

    /**
     * 获取角色选择框列表
     */
    @PrePermission("system:role:query")
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        return AjaxResult.success(roleService.selectRoleAll());
    }

    /**
     * 查询已分配用户角色列表
     */
    @PrePermission("system:role:list")
    @GetMapping("/authUser/allocatedList")
    public TableDataInfo allocatedList(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectAllocatedList(user);
        return getDataTable(list);
    }

    /**
     * 查询未分配用户角色列表
     */
    @PrePermission("system:role:list")
    @GetMapping("/authUser/unallocatedList")
    public TableDataInfo unallocatedList(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectUnallocatedList(user);
        return getDataTable(list);
    }

    /**
     * 取消授权用户
     */
    @PrePermission("system:role:edit")
    @Log(title = "system:role", businessType = BusinessTypeConstants.GRANT)
    @PostMapping("/authUser/cancel")
    public AjaxResult cancelAuthUser(@RequestBody SysUserRole userRole)
    {
        return toAjax(roleService.deleteAuthUser(userRole));
    }

    /**
     * 批量取消授权用户
     */
    @PrePermission("system:role:edit")
    @Log(title = "system:role", businessType = BusinessTypeConstants.GRANT)
    @PostMapping("/authUser/cancelAll")
    public AjaxResult cancelAuthUserAll(@RequestParam("roleId") Long roleId,@RequestParam("userIds") Long[] userIds)
    {
        return toAjax(roleService.deleteAuthUsers(roleId, userIds));
    }

    /**
     * 批量选择用户授权
     */
    @PrePermission("system:role:edit")
    @Log(title = "system:role", businessType = BusinessTypeConstants.GRANT)
    @PostMapping("/authUser/selectAll")
    public AjaxResult selectAuthUserAll(@RequestParam("roleId") Long roleId,@RequestParam("userIds") Long[] userIds)
    {
        return toAjax(roleService.insertAuthUsers(roleId, userIds));
    }



}
