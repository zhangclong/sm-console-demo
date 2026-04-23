package com.uh.console.domain;

import com.uh.util.StringUtils;
import com.uh.common.core.domain.BaseEntity;

/**
 * 配置模版对象 cnsl_template
 *
 * @author Zhang ChenLong
 * @date 2023-01-15
 */
public class Template extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模版ID */
    private Long templateId;

    /** 模版组ID */
    private Long groupId;

    /** 模版名称 */
    private String tempName;

    /** 模版内容 */
    private String tempContent;

    /** 模版类型 */
    private String tempType;

    public Template() {}

    public Template(Long groupId) {
        this.groupId = groupId;
    }

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public Long getTemplateId()
    {
        return templateId;
    }
    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public Long getGroupId()
    {
        return groupId;
    }
    public void setTempName(String tempName)
    {
        this.tempName = tempName;
    }

    public String getTempName()
    {
        return tempName;
    }
    public void setTempContent(String tempContent)
    {
        this.tempContent = tempContent;
    }

    /**
     * 如果存在Windows换行符\r\n，替换为unix的\n
     */
    public void convertTempContent() {
        this.tempContent = StringUtils.convertToUnix(this.tempContent);
    }

    public String getTempContent()
    {
        return tempContent;
    }
    public void setTempType(String tempType)
    {
        this.tempType = tempType;
    }

    public String getTempType()
    {
        return tempType;
    }

    @Override
    public String toString() {
        return "Template{" +
                "templateId=" + templateId +
                ", groupId=" + groupId +
                ", tempName='" + tempName + '\'' +
                ", tempContent='" + tempContent + '\'' +
                ", tempType='" + tempType + '\'' +
                '}';
    }
}
