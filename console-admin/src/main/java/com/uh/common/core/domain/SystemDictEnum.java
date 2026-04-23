package com.uh.common.core.domain;

/**
 * 系统字典枚举基础接口
 * <p>
 * 所有需要暴露给前端的字典枚举都必须实现此接口，并在枚举类上标注 {@link com.uh.common.annotation.DictType}。
 * {@link com.uh.system.manage.DictRegistry} 在 Spring 容器启动时会扫描并装载所有符合条件的枚举。
 * </p>
 *
 * @see com.uh.common.annotation.DictType
 * @see com.uh.system.manage.DictRegistry
 */
public interface SystemDictEnum {

    /**
     * 字典键值，对应 {@code SysDictData.dictValue}，如 {@code "0"}、{@code "1"}。
     */
    String getValue();

    /**
     * 字典标签，对应 {@code SysDictData.dictLabel}，直接返回中文，如 {@code "男"}、{@code "女"}。
     */
    String getLabel();

    /**
     * 表格样式，对应 {@code SysDictData.listClass}，如 {@code "primary"}、{@code "danger"}。
     * 若无需特殊样式，返回空字符串 {@code ""}。
     */
    String getListClass();
}
