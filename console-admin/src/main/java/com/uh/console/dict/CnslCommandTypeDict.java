package com.uh.console.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("cnsl_command_type")
public enum CnslCommandTypeDict implements SystemDictEnum {
    MANAGER("manager", "管理操作", ""),
    NODE("node", "节点操作", "");

    private final String value;
    private final String label;
    private final String listClass;

    CnslCommandTypeDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
