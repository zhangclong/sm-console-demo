package com.uh.console.web;

import java.util.List;

import com.uh.console.domain.TemplateGroup;
import com.uh.console.domain.vo.RdsVersionVo;
import com.uh.console.service.TemplateGroupService;
import com.uh.common.annotation.PrePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.uh.common.annotation.Log;
import com.uh.common.core.controller.BaseController;
import com.uh.common.core.domain.AjaxResult;
import com.uh.common.enums.BusinessTypeConstants;
import com.uh.console.domain.RdsVersion;
import com.uh.console.service.RdsVersionService;
import com.uh.common.core.page.TableDataInfo;

/**
 * 版本信息Controller
 *
 * @author Zhang ChenLong
 * @date 2023-01-12
 */
@RestController
@RequestMapping("/console/rdsversion")
public class RdsVersionController extends BaseController {
    @Autowired
    private RdsVersionService rdsVersionService;

    @Autowired
    private TemplateGroupService templateGroupService;

    /**
     * 查询版本信息列表
     */
    @PrePermission("console:rdsversion:list")
    @GetMapping("/list")
    public TableDataInfo list(RdsVersion rdsVersion) {
        startPage();
        List<RdsVersion> list = rdsVersionService.selectRdsVersionList(rdsVersion);
        return getDataTable(list);
    }

    /**
     * 查询所有版本，by status
     *
     * @param status "1" 启用状态，"0" 停用状态， null 查询所有版本
     * @return
     */
    @GetMapping("/listAll")
    public AjaxResult listAll(@RequestParam(value = "status", required = false) String status) {
        List<RdsVersion> list = rdsVersionService.selectListByStatus(status);
        return AjaxResult.success(list);
    }


    /**
     * 获取版本信息详细信息
     */
    @PrePermission("console:rdsversion:query")
    @GetMapping(value = "/{versionId}")
    public AjaxResult getInfo(@PathVariable("versionId") Long versionId) {
        RdsVersion version = rdsVersionService.selectRdsVersionByVersionId(versionId);
        List<TemplateGroup> tempGroups = templateGroupService.selectGroupByVersionId(versionId);
        RdsVersionVo vo = new RdsVersionVo(version, tempGroups);

        return AjaxResult.success(vo);
    }

    /**
     * 新增版本信息
     */
    @PrePermission("console:rdsversion:add")
    @Log(title = "VersionInformation", businessType = BusinessTypeConstants.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RdsVersion rdsVersion) {
        rdsVersion.setCreateBy(getUsername());
        return toAjax(rdsVersionService.insertRdsVersion(rdsVersion));
    }

    /**
     * 修改版本信息
     */
    @PrePermission("console:rdsversion:edit")
    @Log(title = "VersionInformation", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody RdsVersion rdsVersion) {
        rdsVersion.setUpdateBy(getUsername());
        return toAjax(rdsVersionService.updateRdsVersion(rdsVersion));
    }

    /**
     * 删除版本信息
     */
    @PrePermission("console:rdsversion:remove")
    @Log(title = "VersionInformation", businessType = BusinessTypeConstants.DELETE)
    @GetMapping("/delete/{versionIds}")
    public AjaxResult remove(@PathVariable("versionIds") Long[] versionIds) {
        return toAjax(rdsVersionService.deleteVersionBy(versionIds));
    }


    /**
     * 状态修改
     */
    @PrePermission("console:rdsversion:edit")
    @Log(title = "VersionInformation", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody RdsVersion paramVer) {
        paramVer.setUpdateBy(getUsername());
        return toAjax(rdsVersionService.updateStatus(paramVer));
    }


    /**
     * 改为默认版本
     */
    @PrePermission("console:rdsversion:edit")
    @Log(title = "VersionInformation", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/changeDefault/{versionId}")
    public AjaxResult changeDefault(@PathVariable("versionId") Long versionId) {
        return toAjax(rdsVersionService.updateDefaultVersion(versionId, getUsername()));
    }

    /**
     * 根据安装包类型查询存在的版本信息
     * @param pkgType
     * @return
     */
    @GetMapping("/getPkgTypeVersionList")
    public AjaxResult getPkgTypeVersionList(@RequestParam("pkgType") String pkgType) {
        return AjaxResult.success(rdsVersionService.selectVersionByPkgType(pkgType));
    }
}
