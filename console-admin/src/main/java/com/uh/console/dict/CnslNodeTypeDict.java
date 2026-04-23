package com.uh.console.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("cnsl_node_type")
public enum CnslNodeTypeDict implements SystemDictEnum {
    WORKER("worker", "服务节点", ""),
    CENTER("center", "中心节点", ""),
    SENTINEL("sentinel", "哨兵节点", ""),
    PROXY("proxy", "代理节点", ""),
    EXPORTER("exporter", "监控节点", "");

    private final String value;
    private final String label;
    private final String listClass;

    CnslNodeTypeDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
