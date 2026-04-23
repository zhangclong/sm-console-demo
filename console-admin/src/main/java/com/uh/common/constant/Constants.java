package com.uh.common.constant;

import io.jsonwebtoken.Claims;

/**
 * 通用常量信息
 *
 * @author XiaoZhangTongZhi
 */
public class Constants
{
    /**
     * 超级管理员用户名：admin, uh-admin
     */
    public static final long[] ADMIN_USER_IDS = {1L, 2L, 3L};

    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * www主域
     */
    public static final String WWW = "www.";

    /**
     * http请求
     */
    public static final String HTTP = "http://";

    /**
     * https请求
     */
    public static final String HTTPS = "https://";

    /**
     * 通用成功标识
     */
    public static final String SUCCESS = "0";

    /**
     * 通用失败标识
     */
    public static final String FAIL = "1";

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 注册
     */
    public static final String REGISTER = "Register";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 验证码有效期（分钟）
     */
    public static final Integer CAPTCHA_EXPIRATION = 2;

    /**
     * 令牌
     */
    public static final String TOKEN = "token";

    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌在HTTP Header中的名称
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * 所有权限标识
     */
    public static  final String ALL_PERMISSION = "*:*:*";

    /**
     * 资源映射路径 前缀
     */
    public static final String RESOURCE_PREFIX = "/profile";


    public final static long DAY_MILLIS = 24 * 3600000L;

    /**
     * milliseconds to one hour
     */
    public static final long HOURS_MILLIS = 3600000L;

    /**
     * milliseconds to one minute
     */
    public static final long MINUTE_MILLIS = 60000L;

    /**
     * milliseconds to one one second
     */
    public static final long SECOND_MILLIS = 1000L;

}
