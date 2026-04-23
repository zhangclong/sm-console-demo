package com.uh.console.service;

import java.util.List;
import com.uh.console.domain.TemplateGroup;

/**
 * 配置模版Service接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-16
 */
public interface TemplateGroupService
{
    /**
     * 查询配置模版
     *
     * @param groupId 配置模版主键
     * @return 配置模版
     */
    public TemplateGroup selectTemplateGroupByGroupId(Long groupId);


    /**
     *  查询版本相关配置模版列表
     * @param versionId
     * @return
     */
    public List<TemplateGroup> selectGroupByVersionId(Long versionId);

    /**
     * 查询配置模版列表
     *
     * @param templateGroup 配置模版
     * @return 配置模版集合
     */
    public List<TemplateGroup> selectTemplateGroupList(TemplateGroup templateGroup);

    /**
     * 获得所有配置模版列表(只查询 groupId, groupName两个字段）
     * @return
     */
    List<TemplateGroup>  selectTemplateGroupAll();

    /**
     * 新增配置模版
     *
     * @param templateGroup 配置模版
     * @return 结果
     */
    int insertTemplateGroup(TemplateGroup templateGroup);

    /**
     * 新增配置模版, 加入拷贝自原有配置模版的功能
     * @param templateGroup
     * @return
     */
    int createTemplateGroup(TemplateGroup templateGroup);


    /**
     * 修改配置模版
     *
     * @param templateGroup 配置模版
     * @return 结果
     */
    int updateTemplateGroup(TemplateGroup templateGroup);

    /**
     * 批量删除配置模版
     *
     * @param groupIds 需要删除的配置模版主键集合
     * @return 结果
     */
    int deleteTemplateGroupByGroupIds(Long[] groupIds);

    /**
     * 删除配置模版信息
     *
     * @param groupId 配置模版主键
     * @return 结果
     */
    //int deleteTemplateGroupByGroupId(Long groupId);
}
