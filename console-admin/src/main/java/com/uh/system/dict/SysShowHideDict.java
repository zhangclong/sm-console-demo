package com.uh.system.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("sys_show_hide")
public enum SysShowHideDict implements SystemDictEnum {
    SHOW("0", "显示", "primary"),
    HIDE("1", "隐藏", "danger");

    private final String value;
    private final String label;
    private final String listClass;

    SysShowHideDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
