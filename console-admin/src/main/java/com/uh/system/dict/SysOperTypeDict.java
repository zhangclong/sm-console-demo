package com.uh.system.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("sys_oper_type")
public enum SysOperTypeDict implements SystemDictEnum {
    OTHER("OTHER", "其它", "info"),
    INSERT("INSERT", "新增", "info"),
    UPDATE("UPDATE", "修改", "info"),
    DELETE("DELETE", "删除", "danger"),
    GRANT("GRANT", "授权", "primary"),
    EXPORT("EXPORT", "导出", "warning"),
    IMPORT("IMPORT", "导入", "warning"),
    FORCE("FORCE", "强退", "danger"),
    CLEAN("CLEAN", "清空数据", "danger");

    private final String value;
    private final String label;
    private final String listClass;

    SysOperTypeDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
