package com.uh.system.service;

import com.uh.common.config.ConsoleConfig;
import com.uh.common.config.UserConfig;
import com.uh.common.constant.CacheConstants;
import com.uh.common.constant.Constants;
import com.uh.common.utils.ip.AddressUtils;
import com.uh.common.utils.ip.IpUtils;
import com.uh.common.utils.spring.SpringUtils;
import com.uh.framework.cache.ObjectCache;
import com.uh.common.core.domain.AjaxResult;
import com.uh.common.core.domain.LoginUser;
import com.uh.common.enums.UserStatus;
import com.uh.common.exception.ServiceException;
import com.uh.common.exception.UserBaseException;
import com.uh.common.exception.user.CaptchaException;
import com.uh.common.exception.user.CaptchaExpireException;
import com.uh.common.utils.*;
import com.uh.system.domain.SysLogininfor;
import com.uh.system.domain.SysUser;
import eu.bitwalker.useragentutils.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.uh.common.constant.Constants.DAY_MILLIS;

/**
 * 登录校验方法
 *
 * @author Zhang Chenlong
 */
@Component
public class SysLoginService
{
    private static final Logger log = LoggerFactory.getLogger(SysLoginService.class);

    private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");

    @Resource
    private TokenService tokenService;

    @Resource
    private ObjectCache cacheService;

    @Resource
    private SysUserService userService;

    @Resource
    private SysPermissionService permissionService;

    @Resource
    private UserConfig userConfig;

    @Resource(name = "scheduledExecutorService")
    private ScheduledExecutorService taskExecutor;

    /**
     * 登录验证，返回加密后的令牌
     *   1. 验证验证码
     *   2. 验证用户名和密码（如果验证失败，记入重试次数并验证验证）
     *   3. 如果验证成功，生成登录用户信息（会附加菜单权限），并返回加密后的令牌
     *
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @return  加密后的令牌
     */
    public String login(String username, String password, String code, String uuid)
    {
        // 验证码
        validateCaptcha(username, code, uuid);

        // 用户验证
        LoginUser loginUser = loginValidate(username, password, true);

        recordLogin(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));

        // 如果不允许多终端登录，则删除之前的登录用户信息
        tokenService.delLoginUserByUserId(loginUser.getUserId());

        // 生成token
        return tokenService.addLoginUser(loginUser);
    }

    /**
     * 接口连接时使用的登录验证，不需要验证码，密码也不加解密
     * @param username
     * @param password
     * @return
     */
    public String interfaceLogin(String username, String password) {
        // 用户验证
        LoginUser loginUser = loginValidate(username, password, true);
        loginUser.setInterfaceLogin(true); // 设置为接口登录
        recordLogin(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));

        // 生成token
        return tokenService.addLoginUser(loginUser);
    }

    /**
     * 用户登出操作
     * @return
     */
    public boolean logout() {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        if (StringUtils.isNotNull(loginUser))
        {
            String userName = loginUser.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
            // 记录用户退出日志
            recordLogin(userName, Constants.LOGOUT, "退出成功");

            return true;
        }
        else {
            return false;
        }
    }


    /**
     * 检测用户名/口令是否正确 , 如果错误会抛出异常
     * @param username
     * @param password
     * @return
     */
    public void checkUserPassword(String username, String password)
    {
        loginValidate(username, password, false);
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = cacheService.getCacheObject(verifyKey);
        cacheService.deleteObject(verifyKey);
        if (captcha == null)
        {
            recordLogin(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            recordLogin(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error"));
            throw new CaptchaException();
        }
    }


    /**
     * 返回用户密码修改建议
     * @param userId
     * @return
     *    密码过期： msg="密码已过期，请设置新的密码！"，data="expired";
     *    首次登录： msg="首次登录，为保证您的账号安全，请更改密码！"，data="initial"
     *    无需修改： msg="none", data="none"
     */
    public AjaxResult getPasswordSuggestion(Long userId) {
        SysUser user = userService.selectUserById(userId);
        Date expireDate = user.getPasswordExpired();
        boolean expired = false;
        if(expireDate != null && System.currentTimeMillis() > expireDate.getTime()) {
            expired = true;
        }

        if(user.getUpdateTime() == null) {
            return AjaxResult.success(MessageUtils.message("user.password.initial"), "initial");
        }
        else if(expired) {
            return AjaxResult.success(MessageUtils.message("user.password.expired"), "expired");
        }
        else {
            return AjaxResult.success("none", "none");
        }

    }


    /**
     * 登录验证，进行登录操作。
     *   如果登录成功，会返回 LoginUser 对象；
     *   如果失败抛出异常，并记录失败的登录记录；
     * @param username  用户名
     * @param password  未加密的登录密码
     * @param retryCheck  是否进行重试次数验证，如果验证失败重试次数会累加
     * @return
     */
    private LoginUser loginValidate(String username, String password, boolean retryCheck) {

        // 从数据库获取用户信息，并验证用户信息
        SysUser sysUser = userService.selectUserByUserName(username);
        if (sysUser == null) {
            //log.info("登录用户：{} 不存在.", username);
            throw new ServiceException("user.password.not.match");
        } else if (UserStatus.DELETED.getCode().equals(sysUser.getDelFlag())) {
            //log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException("user.account.delete", username);
        } else if (UserStatus.DISABLE.getCode().equals(sysUser.getStatus())) {
            //log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException("user.account.disabled", username);
        }

        if(retryCheck) {
            //获取系统配置中登录尝试次数账号锁定的值
            int loginRetries = sysUser.getLoginRetries();
            int maxLoginRetries = userConfig.getLoginMaxRetries();

            if (loginRetries >= maxLoginRetries) {
                ServiceException se = new ServiceException("user.login.retries.exceed", maxLoginRetries);
                recordLogin(username, Constants.LOGIN_FAIL, se.getMessage());// 异步日志记录
                throw se;
            } else if (!SecurityUtils.matchesPassword(password, sysUser.getPassword())) {
                SysUser userToUpdate = new SysUser(sysUser.getUserId());
                userToUpdate.setLoginRetries(++loginRetries);
                if (loginRetries >= maxLoginRetries) { //已超出认证重试次数
                    userToUpdate.setLoginLocked("1");
                    userService.updateUser(userToUpdate);
                    ServiceException se = new ServiceException("user.login.retries.exceed", maxLoginRetries);
                    recordLogin(username, Constants.LOGIN_FAIL, se.getMessage());// 异步日志记录
                    throw se;
                } else {   //未到达认证重试次数
                    userToUpdate.setLoginLocked("0");
                    userService.updateUser(userToUpdate);
                    ServiceException se = new ServiceException("user.password.not.match"); //抛出用户名和密码不匹配的异常
                    recordLogin(username, Constants.LOGIN_FAIL, se.getMessage());// 异步日志记录
                    throw se;
                }
            } else {
                clearLoginLocked(sysUser.getUserId());
            }
        }
        else {
            if (!SecurityUtils.matchesPassword(password, sysUser.getPassword())) {
                UserBaseException se = new UserBaseException("user.password.not.match"); //抛出用户名和密码不匹配的异常
                recordLogin(username, Constants.LOGIN_FAIL, se.getMessage());// 异步日志记录
                throw se;
            }
        }

        return  new LoginUser(sysUser, permissionService.getMenuPermission(sysUser));
    }


    /**
     * 清楚登录锁定
     * @param userId
     */
    public void clearLoginLocked(Long userId) {
        SysUser u = new SysUser(userId);
        u.setLoginLocked("0");
        u.setLoginRetries(0);
        userService.updateUser(u);
    }

    /**
     * 已当前时间为准更新到下一个密码过期时间
     * @param userId
     */
    public SysUser renewPasswordExpiredTime(Long userId) {
        SysUser u = new SysUser();
        long expireTime = System.currentTimeMillis() + userConfig.getPasswordExpireDays() * DAY_MILLIS;
        u.setUserId(userId);
        u.setPasswordExpired(new Date(expireTime));
        userService.updateUser(u);
        return u;
    }


    /**
     * 记录登录信息，到数据库的登录日志表中
     *
     * @param username 用户名
     * @param status 状态
     * @param message 消息
     * @param args 列表
     * @return 任务task
     */
    private void recordLogin(final String username, final String status, final String message,
                                        final Object... args)
    {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
        ConsoleConfig consoleConf = SpringUtils.getBean(ConsoleConfig.class);
        taskExecutor.schedule( new TimerTask()
        {
            @Override
            public void run()
            {
                String address = AddressUtils.getRealAddressByIP(ip, consoleConf.isAddressEnabled());
                StringBuilder s = new StringBuilder();
                s.append(LogUtils.getBlock(ip));
                s.append(address);
                s.append(LogUtils.getBlock(username));
                s.append(LogUtils.getBlock(status));
                s.append(LogUtils.getBlock(message));
                // 打印信息到日志
                sys_user_logger.info(s.toString(), args);
                // 获取客户端操作系统
                String os = userAgent.getOperatingSystem().getName();
                // 获取客户端浏览器
                String browser = userAgent.getBrowser().getName();
                // 封装对象
                SysLogininfor logininfor = new SysLogininfor();
                logininfor.setUserName(username);
                logininfor.setIpaddr(ip);
                logininfor.setLoginLocation(address);
                logininfor.setBrowser(browser);
                logininfor.setOs(os);
                logininfor.setMsg(message);
                // 日志状态
                if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER))
                {
                    logininfor.setStatus(Constants.SUCCESS);
                }
                else if (Constants.LOGIN_FAIL.equals(status))
                {
                    logininfor.setStatus(Constants.FAIL);
                }
                // 插入数据
                SpringUtils.getBean(SysLogininforService.class).insertLogininfor(logininfor);

                // 如果登录成功 更新用户登录信息
                if(Constants.LOGIN_SUCCESS.equals(status) || Constants.REGISTER.equals(status)) {
                    SysUser sysUser = new SysUser();
                    sysUser.setUserName(username);
                    sysUser.setLoginIp(ip);
                    sysUser.setLoginDate(DateUtils.getNowDate());
                    SpringUtils.getBean(SysUserService.class).updateLoginInfo(sysUser);
                }

            }
        }, 10, TimeUnit.MILLISECONDS);
    }

}
