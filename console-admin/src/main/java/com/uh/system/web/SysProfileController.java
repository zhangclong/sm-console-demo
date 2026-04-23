package com.uh.system.web;

import com.uh.common.config.AppHomeConfig;
import com.uh.common.utils.AESUtils;
import com.uh.system.service.SysLoginService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.uh.common.annotation.Log;
import com.uh.common.constant.UserConstants;
import com.uh.common.core.controller.BaseController;
import com.uh.common.core.domain.AjaxResult;
import com.uh.system.domain.SysUser;
import com.uh.common.core.domain.LoginUser;
import com.uh.common.enums.BusinessTypeConstants;
import com.uh.common.utils.SecurityUtils;
import com.uh.common.utils.StringUtils;
import com.uh.common.utils.file.FileUploadUtils;
import com.uh.common.utils.file.MimeTypeUtils;
import com.uh.system.service.TokenService;
import com.uh.system.service.SysUserService;

/**
 * 个人信息 业务处理
 *
 * @author XiaoZhangTongZhi
 */
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController
{
    @Autowired
    private SysUserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysLoginService loginService;

    /**
     * 个人信息
     */
    @GetMapping
    public AjaxResult profile()
    {
        LoginUser loginUser = getLoginUser();
        SysUser sysUser = loginUser.getUser();
        SysUser user = new SysUser();
        BeanUtils.copyProperties(sysUser,user);
        user.setPassword("");//不返回密码信息
        AjaxResult ajax = AjaxResult.success(user);
        ajax.put("roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()));
        return ajax;
    }

    /**
     * 修改用户
     */
    @Log(title = "PersonalInformation", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/edit")
    public AjaxResult updateProfile(@RequestBody SysUser user)
    {
        LoginUser loginUser = getLoginUser();
        SysUser sysUser = loginUser.getUser();
        user.setUserName(sysUser.getUserName());
        if (StringUtils.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user)))
        {
            return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user)))
        {
            return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUserId(sysUser.getUserId());
        user.setPassword(null);
        user.setAvatar(null);
        if (userService.updateUserProfile(user) > 0)
        {
            sysUser.setNickName(user.getNickName());
            sysUser.setPhonenumber(user.getPhonenumber());
            sysUser.setEmail(user.getEmail());
            sysUser.setSex(user.getSex());
            tokenService.refreshLoginUser(loginUser);
            return AjaxResult.success();
        }
        return AjaxResult.error("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     */
    @Log(title = "PersonalInformation", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestParam(value = "oldPassword",required = false) String oldPassword, @RequestParam(value = "newPassword",required = false)String newPassword)
    {
        LoginUser loginUser = getLoginUser();
        String userName = loginUser.getUsername();
        String deNewPassword = AESUtils.decryptAES(newPassword);
        String deOldPassword =  AESUtils.decryptAES(oldPassword);

        if (!SecurityUtils.matchesPassword(deOldPassword, loginUser.getPassword()))
        {
            return AjaxResult.error("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(deNewPassword, loginUser.getPassword()))
        {
            return AjaxResult.error("新密码不能与旧密码相同");
        }
        if (userService.resetUserPwd(userName, SecurityUtils.encryptPassword(deNewPassword)) > 0)
        {
            loginUser.getUser().setPassword(SecurityUtils.encryptPassword(deNewPassword));
            SysUser resUser = loginService.renewPasswordExpiredTime(loginUser.getUserId());
            loginUser.getUser().setPasswordExpired(resUser.getPasswordExpired());
            tokenService.refreshLoginUser(loginUser); //刷新缓存中的当前登录信息
            return AjaxResult.success();
        }
        return AjaxResult.error("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     */
    @Log(title = "UserAvatar", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file) throws Exception
    {
        if (!file.isEmpty())
        {
            LoginUser loginUser = getLoginUser();
            String avatar = FileUploadUtils.upload(AppHomeConfig.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION);
            if (userService.updateUserAvatar(loginUser.getUsername(), avatar))
            {
                AjaxResult ajax = AjaxResult.success();
                ajax.put("imgUrl", avatar);
                loginUser.getUser().setAvatar(avatar);
                tokenService.refreshLoginUser(loginUser);
                return ajax;
            }
        }
        return AjaxResult.error("上传图片异常，请联系管理员");
    }
}
