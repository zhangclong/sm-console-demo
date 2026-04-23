package com.uh.console.service.impl;

import com.uh.common.utils.DateUtils;
import com.uh.console.domain.CommandHistory;
import com.uh.console.mapper.CommandHistoryMapper;
import com.uh.console.service.CommandHistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.uh.common.utils.DateUtils.checkDateFormat;

/**
 * 执行命令历史Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-01-20
 */
@Service
public class CommandHistoryServiceImpl implements CommandHistoryService
{
    @Resource
    private CommandHistoryMapper commandHistoryMapper;

    /**
     * 查询执行命令历史
     *
     * @param historyId 执行命令历史主键
     * @return 执行命令历史
     */
    @Override
    public CommandHistory selectCommandHistoryByHistoryId(Long historyId)
    {
        return commandHistoryMapper.selectCommandHistoryByHistoryId(historyId);
    }

    /**
     * 查询执行命令历史列表
     *
     * @param commandHistory 执行命令历史
     * @return 执行命令历史
     */
    @Override
    public List<CommandHistory> selectCommandHistoryList(CommandHistory commandHistory)
    {
        checkDateFormat(commandHistory.getParams(), "beginCreateTime", "endCreateTime");
        return commandHistoryMapper.selectCommandHistoryList(commandHistory);
    }

    /**
     * 获得到最后一次执行的 CommandHistory
     * @param managerId
     * @param command
     * @return
     */
    @Override
    public CommandHistory selectLastCommandHistory(Long managerId, String command) {
        CommandHistory param = new CommandHistory();
        param.setManagerId(managerId);
        param.setCommandType("manager");
        param.setCmd(command);
        return commandHistoryMapper.selectLastCommandHistory(param);
    }


    /**
     * 新增执行命令历史
     *
     * @param commandHistory 执行命令历史
     * @return 结果
     */
    @Override
    public int insertCommandHistory(CommandHistory commandHistory)
    {
        commandHistory.setCreateTime(DateUtils.getNowDate());
        return commandHistoryMapper.insertCommandHistory(commandHistory);
    }

    /**
     * 批量删除执行命令历史
     *
     * @param historyIds 需要删除的执行命令历史主键
     * @return 结果
     */
    @Override
    public int deleteCommandHistoryByHistoryIds(Long[] historyIds)
    {
        return commandHistoryMapper.deleteCommandHistoryByHistoryIds(historyIds);
    }

}
