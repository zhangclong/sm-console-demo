package com.uh.framework.config;

import com.uh.common.config.*;
import com.uh.common.core.YamlConfigManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationYamlConfigurer {

    @Bean
    public ConsoleConfig consoleConfig() {
        return YamlConfigManager.getRootConfig().getConsole();
    }

    @Bean
    public UserConfig userConfig() {
        return YamlConfigManager.getRootConfig().getUser();
    }

    @Bean
    public ServerConfig serverConfig() {
        return YamlConfigManager.getRootConfig().getServer();
    }

    @Bean
    public DataSourceConfig dataSourceConfig() {
        return YamlConfigManager.getRootConfig().getDatasource();
    }

    @Bean
    public ScheduleConfig scheduleConfig() {
        return YamlConfigManager.getRootConfig().getSchedule();
    }

    @Bean
    public LoggingConfig loggingConfig() {
        return YamlConfigManager.getRootConfig().getLogging();
    }

    @Bean
    public InterfaceConfig interfaceConfig() {
        return YamlConfigManager.getRootConfig().getInterface();
    }


}
