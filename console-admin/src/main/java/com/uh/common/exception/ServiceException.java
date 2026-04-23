package com.uh.common.exception;

import com.uh.common.constant.HttpStatus;
import com.uh.common.utils.MessageUtils;

/**
 * 业务异常
 *
 * @author XiaoZhangTongZhi
 */
public final class ServiceException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * HTTP response status
     */
    private Integer httpStatus;

    private String msgCode;

    private Object[] args;

    private Throwable cause;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 空构造方法，避免反序列化问题
     */
    public ServiceException()
    {
    }

    public ServiceException(String msgCode, Object... args)
    {
        this(HttpStatus.ERROR, null, msgCode, args);
    }

    public ServiceException(Throwable cause, String msgCode)
    {
        this(HttpStatus.ERROR, cause, msgCode, null);
    }

    public ServiceException(Integer httpStatus, String msgCode)
    {
        this(httpStatus, null, msgCode, null);
    }

    public ServiceException(Integer httpStatus, Throwable cause, String msgCode, Object... args)
    {
        super(cause);
        this.httpStatus = httpStatus;
        this.cause = cause;
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

    public Integer getHttpStatus() {
        return httpStatus;
    }

}
