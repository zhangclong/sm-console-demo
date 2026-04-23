package com.uh.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字典类型标识注解
 * <p>
 * 标注在实现了 {@link com.uh.common.core.domain.SystemDictEnum} 接口的枚举类上，
 * 声明该枚举映射到前端所使用的 {@code dictType} 键名（如 {@code "sys_user_sex"}）。
 * {@link com.uh.system.manage.DictRegistry} 在 Spring 容器启动时会扫描所有带此注解的枚举并将其装载入内存。
 * </p>
 *
 * @see com.uh.common.core.domain.SystemDictEnum
 * @see com.uh.system.manage.DictRegistry
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DictType {

    /**
     * 字典类型键名，如 {@code "sys_user_sex"}、{@code "sys_normal_disable"}。
     */
    String value();
}
