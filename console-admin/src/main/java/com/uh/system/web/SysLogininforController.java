package com.uh.system.web;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import com.uh.common.annotation.PrePermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.uh.common.annotation.Log;
import com.uh.common.core.controller.BaseController;
import com.uh.common.core.domain.AjaxResult;
import com.uh.common.core.page.TableDataInfo;
import com.uh.common.enums.BusinessTypeConstants;
import com.uh.common.utils.poi.ExcelUtil;
import com.uh.system.domain.SysLogininfor;
import com.uh.system.service.SysLogininforService;

/**
 * 系统访问记录
 *
 * @author XiaoZhangTongZhi
 */
@RestController
@RequestMapping("/monitor/logininfor")
public class SysLogininforController extends BaseController
{
    @Autowired
    private SysLogininforService sysLogininforService;


    @PrePermission("monitor:logininfor:list")
    @GetMapping("/list")
    public TableDataInfo list(SysLogininfor logininfor)
    {
        startPage();
        List<SysLogininfor> list = sysLogininforService.selectLogininforList(logininfor);
        return getDataTable(list);
    }

    @Log(title = "monitor:logininfor", businessType = BusinessTypeConstants.EXPORT)
    @PrePermission("monitor:logininfor:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysLogininfor logininfor)
    {
        List<SysLogininfor> list = sysLogininforService.selectLogininforList(logininfor);
        ExcelUtil<SysLogininfor> util = new ExcelUtil<SysLogininfor>(SysLogininfor.class);
        util.exportExcel(response, list, "登录日志");
    }

    @PrePermission("monitor:logininfor:remove")
    @Log(title = "monitor:logininfor", businessType = BusinessTypeConstants.DELETE)
    @GetMapping("/delete/{infoIds}")
    public AjaxResult remove(@PathVariable("infoIds") Long[] infoIds)
    {
        return toAjax(sysLogininforService.deleteLogininforByIds(infoIds));
    }

    @PrePermission("monitor:logininfor:remove")
    @Log(title = "monitor:logininfor", businessType = BusinessTypeConstants.CLEAN)
    @GetMapping("/clean")
    public AjaxResult clean()
    {
        sysLogininforService.cleanLogininfor();
        return success();
    }

}
