package com.uh.common.config;

import java.io.Serializable;
import java.util.List;

public class ScheduleConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    //核心线程池大小
    private int initPoolSize;
    //最大连接池数量
    private int maxPoolSize;
    //线程池维护线程所允许的空闲时间(秒)
    private int keepAlive;

    // 任务列表
    private List<ScheduleTaskConfig> tasks;

    public int getInitPoolSize() {
        return initPoolSize;
    }

    public void setInitPoolSize(int initPoolSize) {
        this.initPoolSize = initPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public List<ScheduleTaskConfig> getTasks() {
        return tasks;
    }

    public void setTasks(List<ScheduleTaskConfig> tasks) {
        this.tasks = tasks;
    }
}
