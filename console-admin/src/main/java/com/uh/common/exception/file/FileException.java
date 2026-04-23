package com.uh.common.exception.file;

import com.uh.common.exception.UserBaseException;

/**
 * 文件信息异常类
 *
 * @author XiaoZhangTongZhi
 */
public class FileException extends UserBaseException
{
    private static final long serialVersionUID = 1L;

    public FileException(String msgCode, Object[] args)
    {
        super(msgCode, args);
    }

}
