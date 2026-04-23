package com.uh.console.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("cnsl_deploy_mode")
public enum CnslDeployModeDict implements SystemDictEnum {
    SINGLE("single", "单点模式", ""),
    SENTINEL_WORKER("sentinel_worker", "哨兵主从模式", ""),
    CLUSTER("cluster", "集群模式", ""),
    SCALABLE("scalable", "可伸缩集群模式", ""),
    ADJUSTABLE("adjustable", "代理集群模式", ""),
    SENTINEL("sentinel", "哨兵组", ""),
    CENTER("center", "中心服务", ""),
    EXPORTER("exporter", "监控数据接口", "");

    private final String value;
    private final String label;
    private final String listClass;

    CnslDeployModeDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
