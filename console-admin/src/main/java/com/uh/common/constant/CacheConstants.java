package com.uh.common.constant;

/**
 * 缓存的key 常量
 *
 * @author XiaoZhangTongZhi
 */
public class CacheConstants
{
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 登录用户编号 redis key
     */
    public static final String LOGIN_USERID_KEY = "login_userid:";



    public static final String TEMP_CENTER_LICENSE_KEY = "temp_center_license_key";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    public static final String CNSL_TEMPLATE_KEY ="cnsl_template:";
    //public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";
    //public static final String RATE_LIMIT_KEY = "rate_limit:";
    //public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    //public static final String STATIC_RES_KEY = "static_res:";

    public static final String JEDIS_CLIENT = "JEDIS_CLIENT:";

    public static final String OPERATOR_CM = "OPERATOR_RESOURCE_TEMPLATES_CM:";

    //public static final String OPERATOR_CM_LIST = "OPERATOR_RESOURCE_TEMPLATES_CM:";

    public static final String CNSL_LICENSE_SUMMARY_KEY = "cnsl_license_info:summary";
}
