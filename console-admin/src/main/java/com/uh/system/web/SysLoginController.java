package com.uh.system.web;

import com.uh.common.config.InterfaceConfig;
import com.uh.common.constant.Constants;
import com.uh.common.constant.HttpStatus;
import com.uh.common.core.domain.AjaxResult;
import com.uh.common.core.domain.LoginBody;
import com.uh.common.utils.AESUtils;
import com.uh.common.utils.SecurityUtils;
import com.uh.common.utils.ServletUtils;
import com.uh.common.utils.ip.IpUtils;
import com.uh.system.domain.MenuDefinition;
import com.uh.system.domain.SysUser;
import com.uh.system.service.SysLoginService;
import com.uh.system.service.SysPermissionService;
import com.uh.system.service.SystemMenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

import static com.uh.common.utils.StringUtils.isNotEmpty;

/**
 * 登录验证
 *
 * @author XiaoZhangTongZhi
 */
@RestController
public class SysLoginController
{
    private static final Logger logger = LoggerFactory.getLogger(SysLoginController.class);

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private SystemMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Resource
    private InterfaceConfig interfaceConfig;

    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();

        // 通过网页登录，密码要做解密操作
        if(isNotEmpty(loginBody.getPassword())) {
            String password = AESUtils.decryptAES(loginBody.getPassword());
            loginBody.setPassword(password);
        }

        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(), loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    @PostMapping("/logout")
    public AjaxResult logout() {
        boolean succeed = loginService.logout();
        return AjaxResult.error(HttpStatus.SUCCESS,
                succeed ? "Log out successful" : "Already logged out!");
    }

    @PostMapping("/interLogin")
    public AjaxResult interLogin(@RequestBody LoginBody loginBody)
    {
        if(!interfaceConfig.isEnable()) {
            String msg = "Interface login is not enabled!";
            logger.error(msg);
            return AjaxResult.error(msg);
        }

        if(! interfaceConfig.isAllowedUsername(loginBody.getUsername())) {
            String msg = "Username:" + loginBody.getUsername() +" not allowed for interface login!";
            logger.error(msg);
            return AjaxResult.error(msg);
        }


        String clientIp = IpUtils.getIpAddr(ServletUtils.getRequest());
        if(!interfaceConfig.isAllowedIp(clientIp)) {
            String msg = "IP:" + clientIp + " not allowed for interface login!";
            logger.error(msg);
            return AjaxResult.error(msg);
        }

        AjaxResult ajax = AjaxResult.success();

        // 生成令牌
        String token = loginService.interfaceLogin(loginBody.getUsername(), loginBody.getPassword());

        logger.info("Interface login success, user: {}, ip: {}", loginBody.getUsername(), clientIp);

        ajax.put(Constants.TOKEN, token);
        return ajax;
    }


    /**
     * 返回用户密码修改建议
     * @return
     *    密码过期： msg="密码已过期，请设置新的密码！"，data="expired";
     *    首次登录： msg="首次登录，为保证您的账号安全，请更改密码！"，data="initial"
     *    无需修改： msg="none", data="none"
     */
    @GetMapping("/passwordSuggest")
    public AjaxResult passwordSuggest() {
        return loginService.getPasswordSuggestion(SecurityUtils.getUserId());
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/getInfo")
    public AjaxResult getInfo()
    {
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        SysUser user = new SysUser();
        BeanUtils.copyProperties(sysUser,user);
        // user.setPassword("");
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("/getRouters")
    public AjaxResult getRouters()
    {
        Long userId = SecurityUtils.getUserId();
        List<MenuDefinition> menus = menuService.selectMenuDefTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
