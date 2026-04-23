package com.uh.console.domain;

import com.uh.common.core.domain.BaseEntity;

/**
 * 版本信息对象 cnsl_rds_version
 *
 * @author Zhang ChenLong
 * @date 2023-01-12
 */
public class RdsVersion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 版本ID */
    private Long versionId;

    /** 软件名称 */

    private String softwareName;

    /** 版本编号 */

    private String versionNo;

    /** 模版组ID */
    private Long defaultGroupId;

    /** 默认模板组名称(仅用于查询显示） */
    private String defaultGroupName;

    /** 是否为默认版本 */
    private boolean defaultVersion;

    /** 状态（1正常 0停用） */
    private String status;

    private String packageCount;

    public RdsVersion() {
    }

    public RdsVersion(String softwareName, String versionNo) {
        this.softwareName = softwareName;
        this.versionNo = versionNo;
    }

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public Long getVersionId()
    {
        return versionId;
    }
    public void setSoftwareName(String softwareName)
    {
        this.softwareName = softwareName;
    }

    public String getSoftwareName()
    {
        return softwareName;
    }
    public void setVersionNo(String versionNo)
    {
        this.versionNo = versionNo;
    }

    public String getVersionNo()
    {
        return versionNo;
    }

    public String getVersionDesc() {
        if(softwareName != null && versionNo != null) {
            return softwareName + " " + versionNo;
        }
        else {
            return "";
        }
    }

    public boolean isDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(boolean defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public Long getDefaultGroupId() {
        return defaultGroupId;
    }

    public void setDefaultGroupId(Long defaultGroupId) {
        this.defaultGroupId = defaultGroupId;
    }

    public String getDefaultGroupName() {
        return defaultGroupName;
    }

    public void setDefaultGroupName(String defaultGroupName) {
        this.defaultGroupName = defaultGroupName;
    }

    public String getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(String packageCount) {
        this.packageCount = packageCount;
    }

    @Override
    public String toString() {
        return "RdsVersion{" +
                "versionId=" + versionId +
                ", softwareName='" + softwareName + '\'' +
                ", versionNo='" + versionNo + '\'' +
                ", defaultGroupId=" + defaultGroupId +
                ", defaultGroupName='" + defaultGroupName + '\'' +
                ", defaultVersion=" + defaultVersion +
                ", status='" + status + '\'' +
                ", packageCount='" + packageCount + '\'' +
                '}';
    }
}
