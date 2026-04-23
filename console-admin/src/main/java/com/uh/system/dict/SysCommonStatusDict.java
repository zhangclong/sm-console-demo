package com.uh.system.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("sys_common_status")
public enum SysCommonStatusDict implements SystemDictEnum {
    SUCCESS("0", "成功", "primary"),
    FAIL("1", "失败", "danger");

    private final String value;
    private final String label;
    private final String listClass;

    SysCommonStatusDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
