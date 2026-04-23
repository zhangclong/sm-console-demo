package com.uh.common.exception;

/**
 * 在执行SSH操作时的异常
 * 
 * @author uhrongyi
 */
public class SSHProcessException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public SSHProcessException() {
        super();
    }

    public SSHProcessException(String message) {
        super(message);
    }

    public SSHProcessException(Throwable cause) {
        super(cause);
    }

    public SSHProcessException(String message, Throwable e)
    {
        super(message, e);
    }

}
