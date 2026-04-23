package com.uh.common.utils;

import com.uh.common.utils.spring.SpringUtils;
import com.uh.system.service.TokenService;
import com.uh.common.security.BCryptPasswordEncoder;
import com.uh.common.constant.HttpStatus;
import com.uh.common.core.domain.LoginUser;
import com.uh.common.exception.ServiceException;

import static com.uh.common.constant.Constants.ADMIN_USER_IDS;

/**
 * 安全服务工具类
 *
 * @author XiaoZhangTongZhi
 */
public class SecurityUtils
{
    /**
     * 用户ID
     **/
    public static Long getUserId()
    {
        try
        {
            return getLoginUser().getUserId();
        }
        catch(ServiceException serviceException) {
            throw serviceException;
        }
        catch (Exception e)
        {
            throw new ServiceException(HttpStatus.UNAUTHORIZED, e, "user.getUserId.error");
        }
    }


    /**
     * 获取用户账户
     **/
    public static String getUsername()
    {
        try
        {
            return getLoginUser().getUsername();
        }
        catch(ServiceException serviceException) {
            throw serviceException;
        }
        catch (Exception e)
        {
            throw new ServiceException(HttpStatus.UNAUTHORIZED, e, "user.getUsername.error");
        }
    }

    /**
     * 获取用户
     **/
    public static LoginUser getLoginUser()
    {
        TokenService tokenService = SpringUtils.getBean(TokenService.class);
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        if(loginUser == null) {
            throw new ServiceException(HttpStatus.UNAUTHORIZED, "user.getLoginUser.error");
        }
        return loginUser;
    }



    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password)
    {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 判断密码是否相同
     *
     * @param rawPassword 真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword)
    {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 是否为管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isAdmin(Long userId)
    {
        if (userId != null) {
            for (long adminUserId : ADMIN_USER_IDS) {
                if (adminUserId == userId) {
                    return true;
                }
            }
        }
        return false;
    }

}
