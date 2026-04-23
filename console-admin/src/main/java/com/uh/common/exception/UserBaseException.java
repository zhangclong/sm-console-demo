package com.uh.common.exception;

import com.uh.common.utils.MessageUtils;
import com.uh.common.utils.StringUtils;

/**
 * 基础异常
 *
 * @author XiaoZhangTongZhi
 */
public class UserBaseException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private String msgCode;

    private Object[] args;

    private String message;

    public UserBaseException(String msgCode)
    {
        this(msgCode, null);
    }


    public UserBaseException(String msgCode, Object... args)
    {
        this.msgCode = msgCode;
        this.args = args;
        this.message = MessageUtils.message(msgCode, args);
    }


    @Override
    public String getMessage()
    {
        return message;
    }

    public String getMsgCode() {
        return msgCode;
    }

    public Object[] getArgs()
    {
        return args;
    }

}
