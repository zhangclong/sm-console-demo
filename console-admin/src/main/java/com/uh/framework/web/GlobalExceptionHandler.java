package com.uh.framework.web;

import com.uh.common.constant.HttpStatus;
import com.uh.common.core.domain.AjaxResult;
import com.uh.common.exception.UserBaseException;
import com.uh.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

import static com.uh.common.utils.StringUtils.trim;

/**
 * 全局异常处理器
 *
 * @author XiaoZhangTongZhi
 */
@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 权限校验异常
     */
//    @ExceptionHandler(AccessDeniedException.class)
//    public AjaxResult handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request)
//    {
//        String requestURI = request.getRequestURI();
//        log.error("Request URL:'{}', access denied: '{}'", requestURI, e.getMessage());
//        return AjaxResult.error(HttpStatus.FORBIDDEN, "没有权限，请联系管理员授权");
//    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
            HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("Request URL:'{}', Not support method:'{}'", requestURI, e.getMethod());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 业务异常处理，
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e, HttpServletRequest request)
    {
        //如果 getCause ，日志输出 getCause 异常
        if(e.getCause() != null) {
            log.error(e.getCause().getMessage(), e.getCause());
        }
        else {
            log.error(e.getMessage(), e);
        }

        return AjaxResult.error(e.getHttpStatus(), e.getMessage());
    }

    /**
     * 拦截用户提醒类异常
     */
    @ExceptionHandler(UserBaseException.class)
    public AjaxResult handleBaseException(RuntimeException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.warn("Request URL:'{}', UserBaseException type '{}'  message:'{}'"
                , requestURI, e.getClass().getName(), e.getMessage());
        return AjaxResult.error(e.getMessage());
    }


    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("Request URL:'{}', Unknown error exception.", requestURI, e);
        return AjaxResult.error(trim(e.getMessage(), 2, 200));
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("Request URL:'{}', System error exception.", requestURI, e);
        return AjaxResult.error(trim(e.getMessage(), 2, 200));
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public AjaxResult handleBindException(BindException e)
    {
        log.error(e.getMessage(), e);
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e)
    {
        log.error(e.getMessage(), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return AjaxResult.error(message);
    }

}
