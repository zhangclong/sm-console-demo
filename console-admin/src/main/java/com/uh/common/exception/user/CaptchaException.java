package com.uh.common.exception.user;

import com.uh.common.exception.UserBaseException;

/**
 * 验证码错误异常类
 *
 * @author XiaoZhangTongZhi
 */
public class CaptchaException extends UserBaseException
{
    private static final long serialVersionUID = 1L;

    public CaptchaException()
    {
        super("user.jcaptcha.error");
    }
}
