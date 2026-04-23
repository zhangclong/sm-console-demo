package com.uh.console.service.impl;

import com.uh.console.mapper.*;
import com.uh.console.service.DataCleanupService;
import com.uh.system.mapper.SysOperLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 数据清理服务，清除过期的日志和监控数据
 */
@Service
public class DataCleanupServiceImpl implements DataCleanupService {

    @Resource
    private SysOperLogMapper sysOperLogMapper;

    @Resource
    private CommandHistoryMapper commandHistoryMapper;

    @Override
    public int cleanupLogs(Date createTime) {
        int deleted = 0;
        deleted += sysOperLogMapper.deleteByOperTime(createTime);

        //删除Command History
        deleted += commandHistoryMapper.deleteByCreateTime(createTime);

        return deleted;
    }




}
