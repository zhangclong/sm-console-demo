package com.uh.system.manage;

import com.uh.common.config.ScheduleTaskConfig;
import com.uh.common.utils.StringUtils;
import com.uh.common.utils.TaskInvokeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

import static com.uh.common.utils.StringUtils.truncate;

/**
 * 定时任务生成器，根据配置生成对应的定时任务Runnable对象，以及计算初始延迟和周期时间。
 */
public class ScheduledTaskGenerator {

    protected final static Logger logger = LoggerFactory.getLogger("schedule-task");

    private final ScheduleTaskConfig taskConf;

    private TimeUnit timeUnit = null;

    public ScheduledTaskGenerator(ScheduleTaskConfig taskConf) {
        this.taskConf = taskConf;

        timeUnit = TimeUnit.valueOf(taskConf.getUnit());
        if(!(timeUnit == TimeUnit.SECONDS || timeUnit == TimeUnit.MINUTES || timeUnit == TimeUnit.HOURS)) {
            throw new IllegalArgumentException("Invalid time unit: " + timeUnit + ", it should be one of SECONDS, MINUTES, HOURS");
        }
    }

    public Runnable generateTask() {
        return () -> {
            //System.out.println("Task is running..." + taskConf.getName());
            String exceptionMsg = null;
            long startTime = System.currentTimeMillis();
            String invokeTarget = taskConf.getBeanName() + "." + taskConf.getMethod();
            try {
                TaskInvokeUtil.invokeMethod(taskConf);
            } catch (Throwable e) {
                exceptionMsg = TaskInvokeUtil.getStackTrace(e);
                logger.error("Schedule Task {}-{} execute failed: {}", taskConf.getName(), invokeTarget, exceptionMsg);
            }

            // 记录日志
            if(taskConf.isAudit() && exceptionMsg == null) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("Schedule Task {}-{}  execute success, duration: {} ms", taskConf.getName(), invokeTarget, duration);
            }
        };
    }

    /**
     * 返回离下一个定时时间点，还有多少秒
     *    格式为HH:mm:ss 返回离现在最近的几点几分几秒；
     *    格式为mm:ss 返回表示是离现在最近的几分几秒；
     *    格式为为ss 返回离现在最近整数分钟后的几秒；
     *    格式为0 返回离现在最近的整秒；
     * @return
     */
    public long getInitialDelaySeconds() {
        String beginTime = taskConf.getBeginTime();

        int precision = StringUtils.countMatches(beginTime, ":");
        if (precision == 0) {
            return secondsUntilNextMinuteSeconds(beginTime);
        }
        else if (precision == 1) {
            return secondsUntilNextHourTime(beginTime);
        }
        else if (precision == 2) {
            return secondsUntilNextDayTime(beginTime);
        }
        else {
            throw new IllegalArgumentException("Invalid beginTime format: " + beginTime);
        }
    }

    public long getPeriodSeconds() {
        //根据配置的时间单位，返回对应的秒数
        return timeUnit.toSeconds(taskConf.getPeriod());
    }


    /**
     *  格式为HH:mm:ss 返回离现在最近的几点几分几秒,还剩多少秒；
     * @param time
     * @return
     */
    private static long secondsUntilNextDayTime(String time) {
        //解析时间
        String[] parts = time.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid time format: " + time);
        }
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        //获取当前时间
        LocalTime now = LocalTime.now();
        //获取目标时间
        LocalTime targetTime = LocalTime.of(hours, minutes, seconds);

        Duration duration = Duration.between(now, targetTime);
        if (duration.isNegative()) {  //如果目标时间已经过去，则加一天
            duration = duration.plusDays(1);
        }

        return duration.getSeconds();
    }

    /**
     * 格式为mm:ss 返回表示是离现在最近的几分几秒,还剩多少秒；
     * @param time
     * @return
     */
    private static long secondsUntilNextHourTime(String time) {
        //解析时间
        String[] parts = time.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid time format: " + time);
        }
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);

        //获取当前时间
        LocalTime now = LocalTime.now();
        //获取目标时间
        LocalTime targetTime = LocalTime.of(now.getHour(), minutes, seconds);

        Duration duration = Duration.between(now, targetTime);
        if (duration.isNegative()) {  //如果目标时间已经过去，则加一天
            duration = duration.plusHours(1);
        }

        return duration.getSeconds();
    }


    /**
     * 格式为ss 返回表示是离现在最近的 当前分:几秒,还剩多少秒；
     * @param secondsString
     * @return
     */
    private static long secondsUntilNextMinuteSeconds(String secondsString) {
        //解析时间
        int seconds = Integer.parseInt(secondsString);

        //获取当前时间
        LocalTime now = LocalTime.now();
        //获取目标时间
        LocalTime targetTime = LocalTime.of(now.getHour(), now.getMinute(), seconds);

        Duration duration = Duration.between(now, targetTime);
        if (duration.isNegative()) {  //如果目标时间已经过去，则加一天
            duration = duration.plusMinutes(1);
        }

        return duration.getSeconds();
    }

//    public static void main(String[] args) {
//        System.out.println("Seconds until next occurrence of : " + secondsUntilNextDayTime("17:06:10"));
//        System.out.println("Seconds until next occurrence of: " + secondsUntilNextHourTime("19:00"));
//        System.out.println("Seconds until next occurrence of: " + secondsUntilNextMinuteSeconds("00"));
//    }

}
