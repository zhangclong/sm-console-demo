package com.uh.console.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("cnsl_node_managed")
public enum CnslNodeManagedDict implements SystemDictEnum {
    UNMANAGED("0", "未管理", "danger"),
    MANAGED("1", "已管理", "primary"),
    PENDING("2", "待处理", "warning");

    private final String value;
    private final String label;
    private final String listClass;

    CnslNodeManagedDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
