package com.uh.system.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

/**
 * 系统是否字典（sys_yes_no）
 */
@DictType("sys_yes_no")
public enum SysYesNoDict implements SystemDictEnum {

    YES("Y", "是", "primary"),
    NO("N", "否", "danger");

    private final String value;
    private final String label;
    private final String listClass;

    SysYesNoDict(String value, String label, String listClass) {
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
