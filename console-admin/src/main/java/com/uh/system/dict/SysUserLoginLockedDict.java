package com.uh.system.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("sys_user_login_locked")
public enum SysUserLoginLockedDict implements SystemDictEnum {
    NORMAL("0", "正常", "info"),
    LOCKED("1", "锁定", "danger");

    private final String value;
    private final String label;
    private final String listClass;

    SysUserLoginLockedDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
