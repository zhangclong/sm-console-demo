package com.uh.console.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("cnsl_command_result")
public enum CnslCommandResultDict implements SystemDictEnum {
    DONE("done", "成功", ""),
    FAILED("failed", "失败", "");

    private final String value;
    private final String label;
    private final String listClass;

    CnslCommandResultDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
