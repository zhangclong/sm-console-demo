package com.uh.system.service.impl;

import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.uh.system.domain.SysOperLog;
import com.uh.system.domain.MenuDefinition;
import com.uh.system.manage.MenuRegistry;
import com.uh.system.mapper.SysOperLogMapper;
import com.uh.system.service.SysOperLogService;

import static com.uh.common.utils.DateUtils.checkDateFormat;

/**
 * 操作日志 服务层处理
 *
 * @author XiaoZhangTongZhi
 */
@Service
public class SysOperLogServiceImpl implements SysOperLogService
{
    @Autowired
    private SysOperLogMapper operLogMapper;

    @Autowired
    private MenuRegistry menuRegistry;

    /**
     * 新增操作日志
     *
     * @param operLog 操作日志对象
     */
    @Override
    public void insertOperlog(SysOperLog operLog)
    {
        operLogMapper.insertOperlog(operLog);
    }

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    @Override
    public List<SysOperLog> selectOperLogList(SysOperLog operLog)
    {
        checkDateFormat(operLog.getParams(), "beginTime", "endTime");
        List<SysOperLog> operLogs = operLogMapper.selectOperLogList(operLog);
        fillTitleNames(operLogs);
        return operLogs;
    }

    /**
     * 批量删除系统操作日志
     *
     * @param operIds 需要删除的操作日志ID
     * @return 结果
     */
    @Override
    public int deleteOperLogByIds(Long[] operIds)
    {
        return operLogMapper.deleteOperLogByIds(operIds);
    }

    /**
     * 查询操作日志详细
     *
     * @param operId 操作ID
     * @return 操作日志对象
     */
    @Override
    public SysOperLog selectOperLogById(Long operId)
    {
        SysOperLog operLog = operLogMapper.selectOperLogById(operId);
        fillTitleName(operLog);
        return operLog;
    }

    /**
     * 清空操作日志
     */
    @Override
    public void cleanOperLog()
    {
        operLogMapper.cleanOperLog();
    }

    private void fillTitleNames(List<SysOperLog> operLogs)
    {
        if (operLogs == null || operLogs.isEmpty())
        {
            return;
        }
        for (SysOperLog operLog : operLogs)
        {
            fillTitleName(operLog);
        }
    }

    private void fillTitleName(SysOperLog operLog)
    {
        if (operLog == null)
        {
            return;
        }
        operLog.setTitleName(resolveTitleName(operLog.getTitle()));
    }

    private String resolveTitleName(String title)
    {
        MenuDefinition menu = menuRegistry.getByCode(title);
        if (menu == null)
        {
            return title;
        }

        LinkedList<String> menuNames = new LinkedList<>();
        MenuDefinition current = menu;
        while (current != null)
        {
            if (current.getMenuName() != null && !current.getMenuName().isEmpty())
            {
                menuNames.addFirst(current.getMenuName());
            }
            String parentCode = current.getParentCode();
            current = parentCode == null || parentCode.isEmpty() ? null : menuRegistry.getByCode(parentCode);
        }
        return menuNames.isEmpty() ? title : String.join(".", menuNames);
    }
}
