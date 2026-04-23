package com.uh.system.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

/**
 * 用户性别字典（sys_user_sex）
 */
@DictType("sys_user_sex")
public enum SysUserSexDict implements SystemDictEnum {

    MALE("0", "男", ""),
    FEMALE("1", "女", ""),
    UNKNOWN("2", "未知", "");

    private final String value;
    private final String label;
    private final String listClass;

    SysUserSexDict(String value, String label, String listClass) {
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
