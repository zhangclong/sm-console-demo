package com.uh.system.web;

import com.uh.common.annotation.Log;
import com.uh.common.core.controller.BaseController;
import com.uh.common.core.domain.AjaxResult;
import com.uh.common.core.domain.LoginUser;
import com.uh.common.enums.BusinessTypeConstants;
import com.uh.common.utils.SecurityUtils;
import com.uh.system.domain.SysTenant;
import com.uh.system.service.SysTenantService;
import com.uh.system.service.TokenService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租户会话管理接口
 * <p>
 * 提供当前用户的租户查询、默认租户自动选择及手动切换功能。
 * 所有接口均需登录（受 WebConfigFilter 保护）。
 *
 * @author Zhang Chenlong
 */
@RestController
@RequestMapping("/system/tenant")
public class SysTenantController extends BaseController
{
    @Resource
    private SysTenantService tenantService;

    @Resource
    private TokenService tokenService;

    /**
     * 获取当前用户可访问的租户列表。
     * 若用户有 role_key 含 "admin" 的角色则返回所有启用租户，否则返回用户关联的租户。
     *
     * @return 租户列表
     */
    @GetMapping("/my")
    public AjaxResult myTenants()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        List<SysTenant> tenants = tenantService.selectTenantsByUserId(loginUser.getUserId());
        return AjaxResult.success(tenants);
    }

    /**
     * 获取当前会话选中的租户ID。
     * 若 currentTenantId 为 null，则自动选择第一个可用租户并持久化到会话缓存。
     *
     * @return 当前租户ID
     */
    @GetMapping("/current")
    public AjaxResult currentTenant()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser.getCurrentTenantId() == null)
        {
            autoSelectFirstTenant(loginUser);
        }
        return AjaxResult.success(loginUser.getCurrentTenantId());
    }

    /**
     * 切换当前会话的租户。
     * 验证租户ID属于当前用户后，更新会话缓存中的 currentTenantId。
     *
     * @param tenantId 要切换到的租户ID
     * @return 操作结果
     */
    @Log(title = "租户切换", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/select/{tenantId}")
    public AjaxResult selectTenant(@PathVariable Long tenantId)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        tenantService.validateUserTenant(loginUser.getUserId(), tenantId);
        loginUser.setCurrentTenantId(tenantId);
        tokenService.refreshLoginUser(loginUser);
        return AjaxResult.success();
    }

    /**
     * 从用户可访问的租户列表中自动选择第一个租户，并持久化到会话缓存。
     *
     * @param loginUser 当前登录用户
     */
    private void autoSelectFirstTenant(LoginUser loginUser)
    {
        List<Long> tenantIds = tenantService.selectTenantIdsByUserId(loginUser.getUserId());
        if (!tenantIds.isEmpty())
        {
            loginUser.setCurrentTenantId(tenantIds.get(0));
            loginUser.setTenantIds(tenantIds);
            tokenService.refreshLoginUser(loginUser);
        }
    }
}
