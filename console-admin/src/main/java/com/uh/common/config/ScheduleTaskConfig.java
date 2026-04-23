package com.uh.common.config;

import java.io.Serializable;
/**
 * 定时任务的配置
 */
public class ScheduleTaskConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    // 任务名称
    private String name;

    //任务执行周期单位，支持SECONDS,MINUTES,HOURS
    private String unit;

    // 任务执行周期
    private long period;

    // 任务开始时间，如unit为SECONDS, 就是每分钟(0秒)后的第几秒执行
    // 如unit为MINUTES，就是每小时(0分)后的第几分钟执行
    // 如果unit为HOURS，则格式为每天(00:00)后的第几小时执行
    private String beginTime;

    // 任务执行的bean名称
    private String beanName;

    // 任务执行的方法
    private String method;

    // 是否记录审计日志到表 sys_task_log , 此属性默认是true
    private boolean audit = true;

    // 是否启用
    private boolean enable = true;

    // 备注
    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAudit() {
        return audit;
    }

    public void setAudit(boolean audit) {
        this.audit = audit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "ScheduleTaskConfig{" +
                "name='" + name + '\'' +
                ", period=" + period + " " + unit +
                ", beginTime='" + beginTime + '\'' +
                ", invokeTarget='" + beanName + '.' + method + '\'' +
                '}';
    }
}
