package com.uh.console.service.impl;

import com.uh.common.exception.ServiceException;
import com.uh.common.utils.DateUtils;
import com.uh.console.domain.Template;
import com.uh.console.domain.TemplateGroup;
import com.uh.console.domain.TemplateGroupVersion;
import com.uh.console.mapper.RdsVersionMapper;
import com.uh.console.mapper.TemplateGroupMapper;
import com.uh.console.mapper.TemplateGroupVersionMapper;
import com.uh.console.mapper.TemplateMapper;
import com.uh.console.service.TemplateGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.uh.common.constant.ConsoleConstants.DEFAULT_TEMPLATE_GROUP_ID;

/**
 * 配置模版Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-01-16
 */
@Service
public class TemplateGroupServiceImpl implements TemplateGroupService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private TemplateGroupMapper groupMapper;

    @Resource
    private RdsVersionMapper versionMapper;

    @Resource
    private TemplateGroupVersionMapper groupVersionMapper;

    @Resource
    private TemplateMapper templateMapper;

    /**
     * 查询配置模版
     *
     * @param groupId 配置模版主键
     * @return 配置模版
     */
    @Override
    public TemplateGroup selectTemplateGroupByGroupId(Long groupId)
    {
        TemplateGroup group = groupMapper.selectTemplateGroupByGroupId(groupId);
        List<TemplateGroupVersion> groupVersions = groupVersionMapper.selectByGroupId(groupId);
        if(groupVersions != null && groupVersions.size() > 0) {
            int len = groupVersions.size();
            Long[] versionIds = new Long[len];
            for (int i = 0; i < len; i++) {
                versionIds[i] = groupVersions.get(i).getVersionId();
            }
            group.setVersionIds(versionIds);
        }
        else {
            group.setVersionIds(new Long[0]);
        }
        return group;
    }

    @Override
    public List<TemplateGroup> selectGroupByVersionId(Long versionId) {
        return groupMapper.selectTemplateGroupByVersionId(versionId);
    }

    /**
     * 查询配置模版列表
     *
     * @param templateGroup 配置模版
     * @return 配置模版
     */
    @Override
    public List<TemplateGroup> selectTemplateGroupList(TemplateGroup templateGroup)
    {
        return groupMapper.selectTemplateGroupList(templateGroup);
    }

    @Override
    public List<TemplateGroup>  selectTemplateGroupAll() {
        return groupMapper.selectTemplateGroupAll();
    }


    /**
     * 新增配置模版
     *
     * @param group 配置模版
     * @return 结果
     */
    @Override
    @Transactional
    public int insertTemplateGroup(TemplateGroup group)
    {
        group.setCreateTime(DateUtils.getNowDate());
        int ret = groupMapper.insertTemplateGroup(group);

        Long[] versionIds = group.getVersionIds();
        if(versionIds != null && versionIds.length > 0) {
            for(Long versionId : versionIds) {
                TemplateGroupVersion groupVersion = new TemplateGroupVersion(group.getGroupId(), versionId);
                groupVersionMapper.insertGroupVersion(groupVersion);
            }
        }

        return ret;
    }

    @Override
    @Transactional
    public int createTemplateGroup(TemplateGroup templateGroup) {
        int ret = insertTemplateGroup(templateGroup);

        if(ret > 0 && templateGroup.getFromGroupId() != null && templateGroup.getFromGroupId()  > 0) {
            //通过fromGroupId查询出原有的配置模版
            TemplateGroup fromGroup = selectTemplateGroupByGroupId(templateGroup.getFromGroupId());
            if(fromGroup == null) {
                throw new RuntimeException("Failed to find the TemplateGroup by fromGroupId=" + templateGroup.getFromGroupId());
            }

            //拷贝原有的配置模版
            List<Template> templates = templateMapper.selectTemplateListWithContent(new Template(fromGroup.getGroupId()));
            if(templates != null && templates.size() > 0) {
                for(Template template : templates) {
                    template.setTemplateId(null);
                    template.setGroupId(templateGroup.getGroupId());
                    templateMapper.insertTemplate(template);
                }
            }
        }

        return ret;
    }

    /**
     * 修改配置模版
     *
     * @param group 配置模版
     * @return 结果
     */
    @Override
    @Transactional
    public int updateTemplateGroup(TemplateGroup group)
    {
        //删除
        groupVersionMapper.deleteByGroupId(group.getGroupId());

        Long[] versionIds = group.getVersionIds();
        if(versionIds != null && versionIds.length > 0) {
            for(Long versionId : versionIds) {
                TemplateGroupVersion groupVersion = new TemplateGroupVersion(group.getGroupId(), versionId);
                groupVersionMapper.insertGroupVersion(groupVersion);
            }
        }

        group.setUpdateTime(DateUtils.getNowDate());
        return  groupMapper.updateTemplateGroup(group);
    }

    /**
     * 批量删除配置模版
     *
     * @param groupIds 需要删除的配置模版主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteTemplateGroupByGroupIds(Long[] groupIds)
    {
        //检查groupIds是否有默认模版组（DEFAULT_TEMPLATE_GROUP_ID）
        for(Long groupId : groupIds) {
            if(groupId == DEFAULT_TEMPLATE_GROUP_ID) {
                TemplateGroup tg = groupMapper.selectTemplateGroupByGroupId(groupId);
                if(tg != null) {
                    throw new ServiceException("console.template.delete.default.notAllowed", tg.getGroupName());
                }
            }
        }

        int count = 0;
        for(Long groupId : groupIds) {
           if(deleteTemplateGroupByGroupId(groupId) > 0) {
               count ++;
           }
        }
        return count;
    }

    /**
     * 删除配置模版信息
     *
     * @param groupId 配置模版主键
     * @return 结果
     */
    private int deleteTemplateGroupByGroupId(Long groupId)
    {
        versionMapper.clearDefaultGroupId(groupId);
        groupVersionMapper.deleteByGroupId(groupId);
        templateMapper.deleteTemplateByGroupId(groupId);
        return groupMapper.deleteTemplateGroupByGroupId(groupId);
    }
}
