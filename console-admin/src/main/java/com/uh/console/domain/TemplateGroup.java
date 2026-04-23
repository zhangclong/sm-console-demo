package com.uh.console.domain;

import com.uh.common.core.domain.BaseEntity;

import java.util.Arrays;

/**
 * 配置模版对象 cnsl_template_group
 *
 * @author Zhang ChenLong
 * @date 2023-01-16
 */
public class TemplateGroup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模版组ID */
    private Long groupId;

    /** 拷贝自原有的模版组ID，仅在新建时使用 */
    private Long fromGroupId;

    /** 模版组名称 */
    private String groupName;

    /** 版本信息，用于显示 */
    private String versions;

    private Long[] versionIds;

    private String templateCount;

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public Long getGroupId()
    {
        return groupId;
    }

    public Long getFromGroupId() {
        return fromGroupId;
    }

    public void setFromGroupId(Long fromGroupId) {
        this.fromGroupId = fromGroupId;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
    }

    public Long[] getVersionIds() {
        return versionIds;
    }

    public void setVersionIds(Long[] versionIds) {
        this.versionIds = versionIds;
    }

    public String getTemplateCount() {
        return templateCount;
    }

    public void setTemplateCount(String templateCount) {
        this.templateCount = templateCount;
    }

    @Override
    public String toString() {
        return "TemplateGroup{" +
                "groupId=" + groupId +
                ", fromGroupId=" + fromGroupId +
                ", groupName='" + groupName + '\'' +
                ", versions='" + versions + '\'' +
                ", versionIds=" + Arrays.toString(versionIds) +
                ", templateCount='" + templateCount + '\'' +
                '}';
    }
}
