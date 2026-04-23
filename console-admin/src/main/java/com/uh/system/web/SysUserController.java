package com.uh.system.web;

import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.uh.system.service.SysLoginService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.uh.common.annotation.PrePermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.uh.common.annotation.Log;
import com.uh.common.constant.UserConstants;
import com.uh.common.core.controller.BaseController;
import com.uh.common.core.domain.AjaxResult;
import com.uh.system.domain.SysRole;
import com.uh.system.domain.SysUser;
import com.uh.common.core.page.TableDataInfo;
import com.uh.common.enums.BusinessTypeConstants;
import com.uh.common.utils.SecurityUtils;
import com.uh.common.utils.StringUtils;
import com.uh.common.utils.poi.ExcelUtil;
import com.uh.system.service.SysRoleSvc;
import com.uh.system.service.SysUserService;
import com.uh.common.utils.AESUtils;

/**
 * 用户信息
 *
 * @author XiaoZhangTongZhi
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {

    @Autowired
    private SysUserService userService;

    @Autowired
    private SysRoleSvc roleService;

    @Autowired
    private SysLoginService loginService;


    /**
     * 获取用户列表
     */
    @PrePermission("system:user:list")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    @Log(title = "UserManagement", businessType = BusinessTypeConstants.EXPORT)
    @PrePermission("system:user:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUser user) {
        List<SysUser> list = userService.selectUserList(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.exportExcel(response, list, "用户数据");
    }

    @Log(title = "UserManagement", businessType = BusinessTypeConstants.IMPORT)
    @PrePermission("system:user:import")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = userService.importUser(userList, updateSupport, operName);
        return AjaxResult.success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.importTemplateExcel(response, "用户数据");
    }

    /**
     * 根据用户编号获取详细信息
     */
    @PrePermission("system:user:query")
    @GetMapping(value = {"/", "/{userId}"})
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        AjaxResult ajax = AjaxResult.success();
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles", SecurityUtils.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        if (StringUtils.isNotNull(userId)) {
            SysUser sysUser = userService.selectUserById(userId);
            sysUser.setPassword(null); //防止前端获取密码
            ajax.put(AjaxResult.DATA_TAG, sysUser);
            ajax.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
        }
        return ajax;
    }

    /**
     * 新增用户
     */
    @PrePermission("system:user:add")
    @Log(title = "UserManagement", businessType = BusinessTypeConstants.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUser user) {
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user))) {
            return AjaxResult.error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return AjaxResult.error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return AjaxResult.error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setCreateBy(getUsername());

        String password = AESUtils.decryptAES(user.getPassword());


        user.setPassword(SecurityUtils.encryptPassword(password));
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @PrePermission("system:user:edit")
    @Log(title = "UserManagement", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@Validated @RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user))) {
            return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(getUsername());

        if(StringUtils.isNotEmpty(user.getPassword())) {
            String password = AESUtils.decryptAES(user.getPassword());//进行传输解密
            user.setPassword(SecurityUtils.encryptPassword(password));//保存是进行不可逆加密
        }

        return toAjax(userService.updateUserWithRole(user));
    }

    /**
     * 重置密码
     */
    @PrePermission("system:user:resetPwd")
    @Log(title = "UserManagement", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user) {
        // userService.checkUserAllowed(user);
        String password = AESUtils.decryptAES(user.getPassword());

        user.setPassword(SecurityUtils.encryptPassword(password));
        user.setUpdateBy(getUsername());

        int i = userService.resetPwd(user);

        if (i > 0) {
            loginService.renewPasswordExpiredTime(user.getUserId());

        }

        return toAjax(i);
    }

    /**
     * 删除用户
     */
    @PrePermission("system:user:remove")
    @Log(title = "UserManagement", businessType = BusinessTypeConstants.DELETE)
    @GetMapping("/delete/{userIds}")
    public AjaxResult remove(@PathVariable("userIds") Long[] userIds) {
        if (ArrayUtils.contains(userIds, getUserId())) {
            return error("当前用户不能删除");
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }



    /**
     * 状态修改
     */
    @PrePermission("system:user:edit")
    @Log(title = "UserManagement", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 根据用户编号获取授权角色
     */
    @PrePermission("system:user:query")
    @GetMapping("/authRole/{userId}")
    public AjaxResult authRole(@PathVariable("userId") Long userId) {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", SecurityUtils.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return ajax;
    }

    /**
     * 用户授权角色
     */
    @PrePermission("system:user:edit")
    @Log(title = "UserManagement", businessType = BusinessTypeConstants.GRANT)
    @PostMapping("/authRole")
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds) {
        userService.insertUserAuth(userId, roleIds);
        return success();
    }

    @PrePermission("system:user:edit")
    @Log(title = "UserManagement-LoginUnlock", businessType = BusinessTypeConstants.CLEAN)
    @GetMapping("/unlock/{userId}")
    public AjaxResult unlock(@PathVariable("userId") Long userId)
    {
        loginService.clearLoginLocked(userId);
        return success();
    }

}
