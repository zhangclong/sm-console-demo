package com.uh.common.constant;

/**
 * 权限和认证检测相关的网络访问路径。
 */
public class FrameworkConstants {

    // 登录时在可以访问，如果未登录，给出401错误。{"msg":"请求访问：... ，认证失败，无法访问系统资源","code":401}
    public static final String[] PERMIT_LOGINS = {"/web-api/**"};

    // 在 PERMIT_LOGINS 规则内，需要排除现在的地址。
    public static final String[] PERMIT_LOGINS_EXCLUDES = {"/web-api/profile/**", "/web-api/system/config/appConfigKey/**", "/web-api/system/config/appConfigKeys", "/web-api/captchaImage",
            "/web-api/login", "/web-api/interLogin", "/web-api/logout", "/web-api/console/exporterNode/downloadGrafanaTemplate"};

    //只有未登录的状态下才能访问, 登录状态下访问自动 redirect 到主目录
    public static final String[] PERMIT_NOT_LOGINS = {"/login"};

    // 有些访问这些地址会自动定期刷新，刷新token时需要空过这些地址（避免用户未再做任何操作的情况下，页面一直打开不能自动超时退出）。
    public static final String[] TOKEN_REFRESH_SKIPS = {"/web-api/console/checking/result", "/web-api/console/alarm/mail/list"};


    public static final String[] XSS_URL_PATTERNS = {"/system/*", "/monitor/*", "/rds/*", "/log/*"}; // servlet fliter 的路径语法

    public static final String[] XSS_EXCLUDE_PATTERNS = {};   // 需要排除的URL地址规则，AntPathUtils 的路径语法


    public final static long MAX_UPLOAD_SIZE = 100 * 1024 * 1024; //最大上传文件大小 100MB

    public final static long MAX_UPLOAD_SIZE_PER_FILE = 60 * 1024 * 1024; // 单个上传文件的最大大小 60MB

    // MyBatis 别名包路径
    public static final String MYBATIS_TYPE_ALIASES_PACKAGE = "com.uh.console.domain,com.uh.console.domain.vo," +
            "com.uh.system.domain,com.uh.system.domain.vo";
    // MyBatis Mapper XML文件路径
    public static final String MYBATIS_MAPPERS = "classpath*:mapper/**/*Mapper.xml";

    // MyBatis 主配置文件路径
    public static final String MYBATIS_CONFIG = "classpath:mybatis-config.xml";

}
