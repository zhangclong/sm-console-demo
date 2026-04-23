package com.uh.console.service;

import java.util.List;
import com.uh.console.domain.Template;
import com.uh.console.enums.TemplateTypeEnum;

/**
 * 配置模版Service接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-15
 */
public interface TemplateService
{
    /**
     * 查询配置模版
     *
     * @param templateId 配置模版主键
     * @return 配置模版
     */
    public Template selectTemplateByTemplateId(Long templateId);

    /** 根据模版组ID和模版类型找到对应的模版 */
    public Template selectTemplateBy(Long groupId, String type);

    /**
     * 查询配置模版列表
     *
     * @param template 配置模版
     * @return 配置模版集合
     */
    public List<Template> selectTemplateList(Template template);

    /**
     * 新增配置模版
     *
     * @param template 配置模版
     * @return 结果
     */
    public int insertTemplate(Template template);

    /**
     * 修改配置模版
     *
     * @param template 配置模版
     * @return 结果
     */
    public int updateTemplate(Template template);

    /**
     * 批量删除配置模版
     *
     * @param templateIds 需要删除的配置模版主键集合
     * @return 结果
     */
    public int deleteTemplateByTemplateIds(Long[] templateIds);

    /**
     * 删除配置模版信息
     *
     * @param templateId 配置模版主键
     * @return 结果
     */
    public int deleteTemplateByTemplateId(Long templateId);
}
