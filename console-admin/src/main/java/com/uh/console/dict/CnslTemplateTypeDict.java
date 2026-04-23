package com.uh.console.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("cnsl_template_type")
public enum CnslTemplateTypeDict implements SystemDictEnum {
    WORKER_CFG("worker-cfg", "服务节点-配置", ""),
    WORKER_DYNAMIC("worker-dynamic", "服务节点-动态配置", ""),
    WORKER_JVM("worker-jvm", "服务节点-JVM参数", ""),
    SENTINEL_CFG("sentinel-cfg", "哨兵节点-配置", ""),
    SENTINEL_JVM("sentinel-jvm", "哨兵节点-JVM参数", ""),
    PROXY_CFG("proxy-cfg", "代理节点-配置", ""),
    PROXY_DYNAMIC("proxy-dynamic", "代理节点-动态配置", ""),
    PROXY_JVM("proxy-jvm", "代理节点-JVM参数", ""),
    CENTER_CFG("center-cfg", "中心节点-配置", ""),
    CENTER_CLUSTER("center-cluster", "中心节点-部署管理配置", ""),
    CENTER_SYNC("center-sync", "中心节点-同步配置", ""),
    CENTER_ALC("center-alc", "中心节点-用户访问控制", ""),
    CENTER_JVM("center-jvm", "中心节点-JVM参数", "");

    private final String value;
    private final String label;
    private final String listClass;

    CnslTemplateTypeDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
