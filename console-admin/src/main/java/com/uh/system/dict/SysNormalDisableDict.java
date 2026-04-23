package com.uh.system.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

/**
 * 系统开关字典（sys_normal_disable）
 */
@DictType("sys_normal_disable")
public enum SysNormalDisableDict implements SystemDictEnum {

    NORMAL("0", "正常", "primary"),
    DISABLE("1", "停用", "danger");

    private final String value;
    private final String label;
    private final String listClass;

    SysNormalDisableDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getListClass() {
        return listClass;
    }
}
