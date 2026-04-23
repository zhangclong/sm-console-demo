package com.uh.console.domain;

import com.uh.console.enums.CommandResultEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.uh.common.annotation.Excel;
import com.uh.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 执行命令历史对象 cnsl_command_history
 *
 * @author Zhang ChenLong
 * @date 2023-01-20
 */
public class CommandHistory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 历史ID
     */
    private Long historyId;

    /**
     * 操作类型
     */
    @Excel(name = "操作类型")
    private String commandType;

    /**
     * 节点
     */
    @Excel(name = "节点ID")
    private Long nodeId;


    @Excel(name = "节点名称")
    private String nodeName;


    /**
     * 节点管理器
     */
    @Excel(name = "节点管理器ID")
    private Long managerId;

    /**
     * 节点管理器名称
     */
    @Excel(name = "节点管理器名称")
    private String managerName;

    /**
     * 指令
     */
    @Excel(name = "指令")
    private String cmd;

    /**
     * 内容
     */
    @Excel(name = "内容")
    private String cmdMsg;

    /**
     * 文件
     */
    private String cmdFile;

    /**
     * 结果状态
     */
    @Excel(name = "结果状态")
    private String resStatus;

    /**
     * 结果内容
     */
    private String resMsg;

    /**
     * 执行时长(毫秒)
     */
    @Excel(name = "执行时长(毫秒)")
    private Long duration;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;


    public CommandHistory end(CommandResultEnum result, String resultMessage) {
        this.duration = System.currentTimeMillis() - getCreateTime().getTime();
        this.setResStatus(result.getName());
        this.setResMsg(resultMessage);
        return this;
    }

    public static CommandHistory begin(Long nodeId, String cmd, String cmdMsg, Long managerId) {
        CommandHistory his = new CommandHistory();
        his.setNodeId(nodeId);
        his.setCommandType("node");
        his.setCmd(cmd);
        his.setCmdMsg(cmdMsg);
        his.setCreateTime(new Date(System.currentTimeMillis()));
        his.setManagerId(managerId);
        return his;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmdMsg(String cmdMsg) {
        this.cmdMsg = (cmdMsg == null) ? "" :
                (cmdMsg.length() > 9950 ? cmdMsg.substring(0, 9950) : cmdMsg);
    }

    public String getCmdMsg() {
        return cmdMsg;
    }

    public void setCmdFile(String cmdFile) {
        this.cmdFile = cmdFile;
    }

    public String getCmdFile() {
        return cmdFile;
    }

    public void setResStatus(String resStatus) {
        this.resStatus = resStatus;
    }

    public String getResStatus() {
        return resStatus;
    }

    public void setResMsg(String resMsg) {
        this.resMsg = resMsg;
    }

    public String getResMsg() {
        return resMsg;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getDuration() {
        return duration;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("historyId", getHistoryId())
                .append("commandType", getCommandType())
                .append("nodeId", getNodeId())
                .append("managerId", getManagerId())
                .append("cmd", getCmd())
                .append("cmdMsg", getCmdMsg())
                .append("cmdFile", getCmdFile())
                .append("resStatus", getResStatus())
                .append("resMsg", getResMsg())
                .append("createTime", getCreateTime())
                .append("duration", getDuration())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
