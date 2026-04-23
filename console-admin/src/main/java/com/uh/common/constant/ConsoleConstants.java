package com.uh.common.constant;

/**
 * 控制台中通用常量信息
 *
 * @author uhrongyi
 */
public class ConsoleConstants  {

    public static final long CENTER_SERVICE_ID = 1L; //Center服务ID

    public static final long DEFAULT_TEMPLATE_GROUP_ID = 1L; //默认模版组ID

    public static final long EXPORTER_SERVICE_ID = 3L;

    public static final String CONFIG_SYS_INITIALIZED_KEY = "sys.initialized";

    public static final String CONFIG_SYS_DEVELOPMENT_MODE = "sys.developmentMode"; //开发者模式开关

    public static final String CONFIG_CNSL_CENTER_ADMIN_AUTH_KEY = "cnsl.rds.center.admin.authKey"; //Center admin端口的认证Key

    public static final String CONFIG_CNSL_PROBE_AUTH_KEY = "cnsl.probe.authKey"; //Probe端口认证Key

    public static final String CONFIG_CNSL_ALARM_SENDMAIL = "cnsl.alarm.sendMail"; //预警发送邮箱

    public static final String TONG_RDS_LICENSE = "TongRDS";

    public final static String MAIN_ARG_RESTORE = "--restore";

    public final static String MAIN_ARG_BACKUP = "--backup"; //主函数参数，用于自动输出数据库导出文件

    public final static String MAIN_ARG_INITIALIZE = "--initialize";

    public final static String MAIN_ARG_INITIALIZE_SHORT = "-i";

    public final static String MAIN_ENV_START_WITH_INITIALIZE = "console.startWithInitialize"; //系统启动时进行初始化操作

    public final static String UNKNOWN_INSTANCE = "unknown";

    public final static String LOCAL_ADDRESS = "127.0.0.1";

    public static final long PERMANENT_BASIS = 5080636799000L; //license 永久判断依据

    public static final long LICENSE_TYPE_EDUCATION = 1L; //教育版

    public static final int MAX_CENTER_DATA_SIZE = 1024 * 5; //中心数据(Admin端口的监控数据）最大长度

    public static final long CLUSTER_SERVICE_OPERATOR_DELAY = 16000L; //集群服务操作延迟时间，单位：毫秒

}
