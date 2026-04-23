package com.uh.common.annotation;

import java.lang.annotation.*;


/**
 * 权限注解, 定义对应的Controller方法需要的权限。
 *
 * @author XiaoZhangTongZhi
 */
@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PrePermission {

    /**
     * 权限定义标识。如何：system:user:create
     */
    String value();

}
