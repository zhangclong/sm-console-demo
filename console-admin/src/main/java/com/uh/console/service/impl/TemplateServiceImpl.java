package com.uh.console.service.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.uh.framework.cache.ObjectCache;
import com.uh.common.utils.DateUtils;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.uh.console.mapper.TemplateMapper;
import com.uh.console.domain.Template;
import com.uh.console.service.TemplateService;
import org.springframework.transaction.annotation.Transactional;

import static com.uh.common.constant.CacheConstants.CNSL_TEMPLATE_KEY;

/**
 * 配置模版Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-01-15
 */
@Service
public class TemplateServiceImpl implements TemplateService
{
    @Resource
    private TemplateMapper templateMapper;

    @Autowired
    private ObjectCache cache;

    /**
     * 查询配置模版
     *
     * @param templateId 配置模版主键
     * @return 配置模版
     */
    @Override
    public Template selectTemplateByTemplateId(Long templateId)
    {
        return templateMapper.selectTemplateByTemplateId(templateId);
    }


    @Override
    public Template selectTemplateBy(Long groupId, String type) {
        Template ret = cache.getCacheObject(getCacheKey(type, groupId));
        if(ret == null) {
            Template param = new Template();
            param.setGroupId(groupId);
            param.setTempType(type);
            ret = templateMapper.selectTemplateByGroupType(param);
            cache.setCacheObject(getCacheKey(type, groupId), ret, 20, TimeUnit.MINUTES); //缓存20分钟
        }
        return ret;
    }


    /**
     * 查询配置模版列表
     *
     * @param template 配置模版
     * @return 配置模版
     */
    @Override
    public List<Template> selectTemplateList(Template template)
    {
        return templateMapper.selectTemplateList(template);
    }

    /**
     * 新增配置模版
     *
     * @param template 配置模版
     * @return 结果
     */
    @Override
    public int insertTemplate(Template template)
    {
        template.setCreateTime(DateUtils.getNowDate());
        template.convertTempContent();
        return templateMapper.insertTemplate(template);
    }

    /**
     * 修改配置模版
     *
     * @param template 配置模版
     * @return 结果
     */
    @Override
    public int updateTemplate(Template template)
    {
        deleteCache(template.getTemplateId());
        template.setUpdateTime(DateUtils.getNowDate());
        template.convertTempContent();
        return templateMapper.updateTemplate(template);
    }

    /**
     * 批量删除配置模版
     *
     * @param templateIds 需要删除的配置模版主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteTemplateByTemplateIds(Long[] templateIds)
    {
        for(Long templateId : templateIds) {
            deleteCache(templateId);
        }
        return templateMapper.deleteTemplateByTemplateIds(templateIds);
    }

    /**
     * 删除配置模版信息
     *
     * @param templateId 配置模版主键
     * @return 结果
     */
    @Override
    public int deleteTemplateByTemplateId(Long templateId)
    {
        deleteCache(templateId);
        return templateMapper.deleteTemplateByTemplateId(templateId);
    }




    private void deleteCache(Long templateId) {
        cache.deleteObject(getCacheKey(templateId));
    }

    private String getCacheKey(Long templateId) {
        String typeKey = null;
        Template t = templateMapper.selectTemplateByTemplateId(templateId);
        if(t != null) {
            typeKey = getCacheKey(t.getTempType(), t.getGroupId());
        }
        return typeKey;
    }

    private String getCacheKey(String type, Long groupId)
    {
        return CNSL_TEMPLATE_KEY + type + groupId;
    }
}
