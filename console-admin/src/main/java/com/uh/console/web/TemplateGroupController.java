package com.uh.console.web;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.uh.console.domain.TemplateGroup;
import com.uh.console.service.TemplateGroupService;
import com.uh.common.utils.poi.ExcelUtil;
import com.uh.common.core.page.TableDataInfo;

/**
 * 配置模版Controller
 *
 * @author Zhang ChenLong
 * @date 2023-01-16
 */
@RestController
@RequestMapping("/console/tempgroup")
public class TemplateGroupController extends BaseController
{
    @Autowired
    private TemplateGroupService templateGroupService;

    /**
     * 查询配置模版列表
     */
    @PrePermission("console:tempgroup:list")
    @GetMapping("/list")
    public TableDataInfo list(TemplateGroup templateGroup)
    {
        startPage();
        List<TemplateGroup> list = templateGroupService.selectTemplateGroupList(templateGroup);
        return getDataTable(list);
    }


    /**
     * 查询配置模版列表
     */
    @PrePermission("console:tempgroup:list")
    @GetMapping("/listAll")
    public AjaxResult listAll()
    {
        return AjaxResult.success(templateGroupService.selectTemplateGroupAll());
    }


    @PrePermission("console:tempgroup:list")
    @GetMapping(value = "/listByVersionId/{versionId}")
    public AjaxResult listByVersionId(@PathVariable("versionId") Long versionId) {
        List<TemplateGroup> tempGroups = templateGroupService.selectGroupByVersionId(versionId);
        return AjaxResult.success(tempGroups);
    }


    /**
     * 导出配置模版列表
     */
    @PrePermission("console:tempgroup:export")
    @Log(title = "console:tempgroup", businessType = BusinessTypeConstants.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TemplateGroup templateGroup)
    {
        List<TemplateGroup> list = templateGroupService.selectTemplateGroupList(templateGroup);
        ExcelUtil<TemplateGroup> util = new ExcelUtil<TemplateGroup>(TemplateGroup.class);
        util.exportExcel(response, list, "配置模版数据");
    }

    /**
     * 获取配置模版详细信息
     */
    @PrePermission("console:tempgroup:query")
    @GetMapping(value = "/{groupId}")
    public AjaxResult getInfo(@PathVariable("groupId") Long groupId)
    {
        return AjaxResult.success(templateGroupService.selectTemplateGroupByGroupId(groupId));
    }

    /**
     * 新增配置模版
     */
    @PrePermission("console:tempgroup:add")
    @Log(title = "console:tempgroup", businessType = BusinessTypeConstants.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TemplateGroup templateGroup)
    {
        return toAjax(templateGroupService.createTemplateGroup(templateGroup));
    }

    /**
     * 修改配置模版
     */
    @PrePermission("console:tempgroup:edit")
    @Log(title = "console:tempgroup", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody TemplateGroup templateGroup)
    {
        return toAjax(templateGroupService.updateTemplateGroup(templateGroup));
    }

    /**
     * 删除配置模版
     */
    @PrePermission("console:tempgroup:remove")
    @Log(title = "console:tempgroup", businessType = BusinessTypeConstants.DELETE)
	@GetMapping("/delete/{groupIds}")
    public AjaxResult remove(@PathVariable("groupIds") Long[] groupIds)
    {
        return toAjax(templateGroupService.deleteTemplateGroupByGroupIds(groupIds));
    }
}
