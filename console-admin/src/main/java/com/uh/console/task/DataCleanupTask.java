package com.uh.console.task;

import com.uh.console.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static javax.management.timer.Timer.ONE_HOUR;
import static javax.management.timer.Timer.ONE_MINUTE;

@Component("dataCleanupTask")
public class DataCleanupTask {

    protected final Logger logger = LoggerFactory.getLogger(DataCleanupTask.class);

    @Autowired
    private DataCleanupService cleanupService;

    /**
     * 删除hoursAgo小时前的, 日志数据
     * 包括表：cnsl_command_history, sys_oper_log(oper_time), sys_task_log
     * @param hoursAgo
     */
    public synchronized void cleanLogs(Integer hoursAgo) {
        Date timeAgo = new Date(System.currentTimeMillis() - ONE_HOUR * hoursAgo);
        cleanupService.cleanupLogs(timeAgo);
        logger.info("cleanLogs(" + hoursAgo + "hours), clean time before:" + timeAgo);
    }

}
