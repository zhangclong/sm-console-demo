package com.uh.common.config;



/**
 * 应用程序在初始化时加载的总配置类
 */
public class ApplicationRootConfig {

    private ConsoleConfig console;
    private UserConfig user;
    private ServerConfig server;
    private DataSourceConfig datasource;
    private ScheduleConfig schedule;
    private LoggingConfig logging;
    private InterfaceConfig interfaceConfig; // 'interface' is a reserved word in Java, getters/setters will still use "interface"


    public ConsoleConfig getConsole() {
        return console;
    }
    public void setConsole(ConsoleConfig console) {
        this.console = console;
    }
    public UserConfig getUser() {
        return user;
    }
    public void setUser(UserConfig user) {
        this.user = user;
    }

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public DataSourceConfig getDatasource() {
        return datasource;
    }

    public void setDatasource(DataSourceConfig datasource) {
        this.datasource = datasource;
    }

    public ScheduleConfig getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleConfig schedule) {
        this.schedule = schedule;
    }

    public LoggingConfig getLogging() {
        return logging;
    }

    public void setLogging(LoggingConfig logging) {
        this.logging = logging;
    }

    public InterfaceConfig getInterface() {
        return interfaceConfig;
    }

    public void setInterface(InterfaceConfig interfaceConfig) {
        this.interfaceConfig = interfaceConfig;
    }


}
