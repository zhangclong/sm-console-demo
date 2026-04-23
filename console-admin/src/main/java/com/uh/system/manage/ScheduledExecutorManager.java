package com.uh.system.manage;

import com.uh.common.config.ScheduleConfig;
import com.uh.common.config.ScheduleTaskConfig;
import com.uh.common.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务管理器，用于加载和启动系统中周期性定时任务。
 */
@Component
public class ScheduledExecutorManager {

    private static final Logger logger = LoggerFactory.getLogger("schedule-task");

    @Resource(name = "scheduledExecutorService")
    private ScheduledExecutorService scheduledExecutorService;

    @Resource
    private ScheduleConfig scheduleConfig;

    @PostConstruct
    public void initScheduleTasks() {
        //从配置文件加载定时任务
        for(ScheduleTaskConfig task: scheduleConfig.getTasks()) {
            if(task.isEnable()) {
                ScheduledTaskGenerator gen = new ScheduledTaskGenerator(task);
                scheduledExecutorService.scheduleAtFixedRate(gen.generateTask(), gen.getInitialDelaySeconds(), gen.getPeriodSeconds(), TimeUnit.SECONDS);
                logger.info("Schedule task added: {}", task);
            }
        }
    }


    @PreDestroy
    public void destroy()
    {
        try {
            logger.info("Shutdown scheduledExecutorService ...");
            ThreadUtils.shutdownAndAwaitTermination(scheduledExecutorService);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
