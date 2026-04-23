package com.uh.system.service.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.uh.common.config.*;
import com.uh.common.constant.FrameworkConstants;
import com.uh.common.core.YamlConfigManager;
import com.uh.system.service.MySqlDatabaseInitializer;
import com.uh.common.utils.spring.SpringUtils;
import com.uh.ConsoleApplication;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.*;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 测试用 Spring 上下文配置，用于 Service 层集成测试。
 * <p>
 * 复用主应用的 YamlConfigManager 和 H2 数据库，不依赖 Spring Boot。
 * 使用方式：@ContextConfiguration(classes = TestAppConfig.class)
 * 测试工作目录需为 apphome（Surefire 已配置）。
 */
@Configuration
@ComponentScan(basePackages = {
        "com.uh.system.service",
        "com.uh.system.manage",
        "com.uh.framework.cache.impl"
})
@MapperScan("com.uh.**.mapper")
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestAppConfig {

    static {
        // 在 Spring 容器启动前初始化 YamlConfigManager（工作目录为 apphome），并确保 MySQL 数据库已初始化。
        // 静态初始化块由 JVM 类加载保证只执行一次，线程安全。
        ApplicationRootConfig rootConfig = YamlConfigManager.loadConfig(ConsoleApplication.CONFIG_FILES);
        try {
            DataSourceConfig ds = rootConfig.getDatasource();
            Class.forName(ds.getDriverClassName());
            MySqlDatabaseInitializer initializer = new MySqlDatabaseInitializer(
                    ds.getUrl(),
                    ds.getUsername(),
                    ds.getPassword(),
                    rootConfig.getConsole().getAdminPasswordExpireDays());
            initializer.initializeIfAbsent();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * 在所有 @PostConstruct 方法执行前将 ApplicationContext 注入 SpringUtils。
     * static @Bean 方法确保该 BeanFactoryPostProcessor 在常规 Bean 之前被创建。
     */
    @Bean
    public static EarlySpringUtilsInitializer earlySpringUtilsInitializer() {
        return new EarlySpringUtilsInitializer();
    }

    /**
     * 在 BeanFactoryPostProcessor 阶段初始化 SpringUtils，
     * 确保后续 @PostConstruct（如 TokenService.init()）可正常调用 SpringUtils.getBean()。
     */
    public static class EarlySpringUtilsInitializer
            implements BeanFactoryPostProcessor, ApplicationContextAware {

        private ApplicationContext applicationContext;

        @Override
        public void setApplicationContext(ApplicationContext ctx) {
            this.applicationContext = ctx;
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            if (applicationContext != null) {
                SpringUtils.setApplicationContext(applicationContext);
            }
        }
    }

    @Bean
    public ConsoleConfig consoleConfig() {
        return YamlConfigManager.getRootConfig().getConsole();
    }

    @Bean
    public UserConfig userConfig() {
        return YamlConfigManager.getRootConfig().getUser();
    }

    @Bean
    public ScheduleConfig scheduleConfig() {
        return YamlConfigManager.getRootConfig().getSchedule();
    }

    @Bean
    public DataSourceConfig dataSourceConfig() {
        return YamlConfigManager.getRootConfig().getDatasource();
    }

    @Bean
    public InterfaceConfig interfaceConfig() {
        InterfaceConfig cfg = YamlConfigManager.getRootConfig().getInterface();
        if (cfg == null) {
            cfg = new InterfaceConfig();
        }
        return cfg;
    }

    @Bean
    public DataSource dataSource(DataSourceConfig conf) throws Exception {
        ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setDriverClass(conf.getDriverClassName());
        ds.setJdbcUrl(conf.getUrl());
        ds.setUser(conf.getUsername());
        ds.setPassword(conf.getPassword());
        ds.setInitialPoolSize(2);
        ds.setMinPoolSize(2);
        ds.setMaxPoolSize(5);
        ds.setAcquireRetryAttempts(1);
        ds.setMaxIdleTime(60);
        ds.setMaxStatements(10);
        ds.setAcquireIncrement(1);
        ds.setIdleConnectionTestPeriod(30);
        return ds;
    }

    @Bean
    public org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setTypeAliasesPackage(FrameworkConstants.MYBATIS_TYPE_ALIASES_PACKAGE);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factoryBean.setMapperLocations(resolver.getResources(FrameworkConstants.MYBATIS_MAPPERS));
        factoryBean.setConfigLocation(new DefaultResourceLoader().getResource(FrameworkConstants.MYBATIS_CONFIG));
        factoryBean.setDataSource(dataSource);
        return factoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public TransactionManagementConfigurer transactionManagementConfigurer(PlatformTransactionManager txManager) {
        return () -> txManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager txManager) {
        TransactionTemplate template = new TransactionTemplate(txManager);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return template;
    }

    @Bean(name = "scheduledExecutorService")
    public ScheduledExecutorService scheduledExecutorService(ScheduleConfig config) {
        return new ScheduledThreadPoolExecutor(
                Math.max(2, config.getInitPoolSize()),
                new BasicThreadFactory.Builder()
                        .namingPattern("test-schedule-%d")
                        .daemon(true)
                        .build()
        );
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getDefault());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
