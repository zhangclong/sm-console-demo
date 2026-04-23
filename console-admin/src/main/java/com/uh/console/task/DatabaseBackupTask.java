package com.uh.console.task;

import com.uh.system.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("databaseBackupTask")
public class DatabaseBackupTask {

    protected final Logger logger = LoggerFactory.getLogger(DatabaseBackupTask.class);

    @Resource
    private DatabaseService backupService;

    public void process(Integer maxHistoryDay) {
        backupService.cleanExpiredBackup(maxHistoryDay);
        backupService.backupDatabase();
    }

}


