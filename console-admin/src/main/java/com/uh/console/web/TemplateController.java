package com.uh.console.web;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.uh.common.utils.MessageUtils;
import com.uh.common.annotation.PrePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.uh.common.annotation.Log;
import com.uh.common.core.controller.BaseController;
import com.uh.common.core.domain.AjaxResult;
import com.uh.common.enums.BusinessTypeConstants;
import com.uh.console.domain.Template;
import com.uh.console.service.TemplateService;
import com.uh.common.utils.poi.ExcelUtil;
import com.uh.common.core.page.TableDataInfo;

/**
 * 配置模版Controller
 *
 * @author Zhang ChenLong
 * @date 2023-01-15
 */
@RestController
@RequestMapping("/console/template")
public class TemplateController extends BaseController
{
    @Autowired
    private TemplateService templateService;

    /**
     * 查询配置模版列表
     */
    @PrePermission("console:tempgroup:list")
    @GetMapping("/list")
    public TableDataInfo list(Template template)
    {
        startPage();
        List<Template> list = templateService.selectTemplateList(template);
        return getDataTable(list);
    }

    /**
     * 导出配置模版列表
     */
    @PrePermission("console:tempgroup:export")
    @Log(title = "ConfigureTheTemplate", businessType = BusinessTypeConstants.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Template template)
    {
        List<Template> list = templateService.selectTemplateList(template);
        ExcelUtil<Template> util = new ExcelUtil<Template>(Template.class);
        util.exportExcel(response, list, "配置模版数据");
    }

    /**
     * 获取配置模版详细信息
     */
    @PrePermission("console:tempgroup:query")
    @GetMapping(value = "/{templateId}")
    public AjaxResult getInfo(@PathVariable("templateId") Long templateId)
    {
        return AjaxResult.success(templateService.selectTemplateByTemplateId(templateId));
    }

    /**
     * 新增配置模版
     */
    @PrePermission("console:tempgroup:add")
    @Log(title = "ConfigureTheTemplate", businessType = BusinessTypeConstants.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Template template)
    {
        Template param = new Template();
        param.setGroupId(template.getGroupId());
        param.setTempType(template.getTempType());

        List<Template> list = templateService.selectTemplateList(param);
        if(list.size() >= 1) {
            return AjaxResult.error(MessageUtils.message("console.template.error.repeat"));
        }

        String content = template.getTempContent();
        if(content != null && content.contains("<!ENTITY")) {
            return AjaxResult.error(MessageUtils.message("console.template.error.IllegalTags"));
        }

        return toAjax(templateService.insertTemplate(template));
    }

    /**
     * 修改配置模版
     */
    @PrePermission("console:tempgroup:edit")
    @Log(title = "ConfigureTheTemplate", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody Template template)
    {
        String content = template.getTempContent();
        if(content != null && content.contains("<!ENTITY")) {
            return AjaxResult.error(MessageUtils.message("console.template.error.IllegalTags"));
        }

        return toAjax(templateService.updateTemplate(template));
    }

    /**
     * 删除配置模版
     */
    @PrePermission("console:tempgroup:remove")
    @Log(title = "ConfigureTheTemplate", businessType = BusinessTypeConstants.DELETE)
	@GetMapping("/delete/{templateIds}")
    public AjaxResult remove(@PathVariable("templateIds") Long[] templateIds)
    {
        return toAjax(templateService.deleteTemplateByTemplateIds(templateIds));
    }
}
