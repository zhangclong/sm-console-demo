package com.uh.console.service;

import com.uh.console.domain.CommandHistory;

import java.util.List;

/**
 * 执行命令历史Service接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-20
 */
public interface CommandHistoryService
{
    /**
     * 查询执行命令历史
     *
     * @param historyId 执行命令历史主键
     * @return 执行命令历史
     */
    public CommandHistory selectCommandHistoryByHistoryId(Long historyId);

    /**
     * 查询执行命令历史列表
     *
     * @param commandHistory 执行命令历史
     * @return 执行命令历史集合
     */
    public List<CommandHistory> selectCommandHistoryList(CommandHistory commandHistory);

    /**
     * 获得到最后一次执行的 CommandHistory
     * @param managerId
     * @param command
     * @return
     */
    public CommandHistory selectLastCommandHistory(Long managerId, String command);

    /**
     * 新增执行命令历史
     *
     * @param commandHistory 执行命令历史
     * @return 结果
     */
    public int insertCommandHistory(CommandHistory commandHistory);

    /**
     * 批量删除执行命令历史
     *
     * @param historyIds 需要删除的执行命令历史主键集合
     * @return 结果
     */
    public int deleteCommandHistoryByHistoryIds(Long[] historyIds);

}
