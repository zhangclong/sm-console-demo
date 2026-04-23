package com.uh.common.exception.user;

import com.uh.common.exception.UserBaseException;

/**
 * 验证码失效异常类
 *
 * @author XiaoZhangTongZhi
 */
public class CaptchaExpireException extends UserBaseException
{
    private static final long serialVersionUID = 1L;

    public CaptchaExpireException()
    {
        super("user.jcaptcha.expire");
    }
}
