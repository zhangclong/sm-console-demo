package com.uh.console.mapper;

import java.util.Date;
import java.util.List;

import com.uh.common.core.TimeDeletable;
import com.uh.console.domain.CommandHistory;

/**
 * 执行命令历史Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-20
 */
public interface CommandHistoryMapper extends TimeDeletable
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
     * 获得到最后一次执行的 CommandHistory，返回的属性字段中，去掉了 cmdMsg, cmdFile, resMsg 等大字段。
     *
     * @param commandHistory   查询条件，其中属性 commandType，commandType，nodeId，managerId，cmd可作为查询条件
     * @return
     */
    public CommandHistory selectLastCommandHistory(CommandHistory commandHistory);

    /**
     * 新增执行命令历史
     *
     * @param commandHistory 执行命令历史
     * @return 结果
     */
    public int insertCommandHistory(CommandHistory commandHistory);

    /**
     * 修改执行命令历史
     *
     * @param commandHistory 执行命令历史
     * @return 结果
     */
    public int updateCommandHistory(CommandHistory commandHistory);


    /**
     * 批量删除执行命令历史
     *
     * @param historyIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCommandHistoryByHistoryIds(Long[] historyIds);

    /**
     * 删除执行命令历史,通过节点ID
     *
     * @param nodeId 执行命令历史主键
     * @return 结果
     */
    public int deleteCommandHistoryByNodeId(Long nodeId);

    /**
     * 删除小于 createTime 参数时间之前的数据
     * @param createTime
     * @return
     */
    public int deleteByCreateTime(Date createTime);


}
