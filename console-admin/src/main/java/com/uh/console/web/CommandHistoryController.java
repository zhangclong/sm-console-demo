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
import com.uh.console.domain.CommandHistory;
import com.uh.console.service.CommandHistoryService;
import com.uh.common.utils.poi.ExcelUtil;
import com.uh.common.core.page.TableDataInfo;

/**
 * 执行命令历史Controller
 *
 * @author Zhang ChenLong
 * @date 2023-01-20
 */
@RestController
@RequestMapping("/console/commandhistory")
public class CommandHistoryController extends BaseController
{
    @Autowired
    private CommandHistoryService commandHistoryService;

    /**
     * 查询执行命令历史列表
     */
    @PrePermission("console:commandhistory:list")
    @GetMapping("/list")
    public TableDataInfo list(CommandHistory commandHistory)
    {
        startPage();
        List<CommandHistory> list = commandHistoryService.selectCommandHistoryList(commandHistory);
        return getDataTable(list);
    }

    /**
     * 导出执行命令历史列表
     */
    @PrePermission("console:commandhistory:export")
    @Log(title = "console:commandhistory", businessType = BusinessTypeConstants.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CommandHistory commandHistory)
    {
        List<CommandHistory> list = commandHistoryService.selectCommandHistoryList(commandHistory);
        ExcelUtil<CommandHistory> util = new ExcelUtil<CommandHistory>(CommandHistory.class);
        util.exportExcel(response, list, "执行命令历史数据");
    }

    /**
     * 获取执行命令历史详细信息
     */
    @PrePermission("console:commandhistory:query")
    @GetMapping(value = "/{historyId}")
    public AjaxResult getInfo(@PathVariable("historyId") Long historyId)
    {
        return AjaxResult.success(commandHistoryService.selectCommandHistoryByHistoryId(historyId));
    }

    /**
     * 删除执行命令历史
     */
    @PrePermission("console:commandhistory:remove")
    @Log(title = "console:commandhistory", businessType = BusinessTypeConstants.DELETE)
	@GetMapping("/delete/{historyIds}")
    public AjaxResult remove(@PathVariable("historyIds") Long[] historyIds)
    {
        return toAjax(commandHistoryService.deleteCommandHistoryByHistoryIds(historyIds));
    }
}
