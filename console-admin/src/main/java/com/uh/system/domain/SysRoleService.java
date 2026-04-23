package com.uh.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 角色和服务关联 sys_role_service
 * @author Zhang Chenlong
 */
public class SysRoleService
{
    /** 角色ID */
    private Long roleId;

    /** 服务ID */
    private Long serviceId;

    public SysRoleService() {
    }

    public SysRoleService(Long roleId, Long serviceId) {
        this.roleId = roleId;
        this.serviceId = serviceId;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("roleId", getRoleId())
            .append("serviceId", getServiceId())
            .toString();
    }
}
