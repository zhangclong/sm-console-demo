package com.uh;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

import com.uh.common.config.*;
import com.uh.common.constant.FrameworkConstants;
import com.uh.common.core.WebServerStarter;
import com.uh.common.core.YamlConfigManager;
import com.uh.common.utils.spring.SpringUtils;
import com.uh.framework.config.ApplicationMvcConfigurer;
import com.uh.framework.web.StaticResourceServlet;
import com.uh.framework.web.WebConfigFilter;
import com.uh.framework.web.XssFilter;
import com.uh.system.service.DatabaseService;
import com.uh.system.service.MySqlDatabaseInitializer;
import com.uh.system.service.impl.DatabaseServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.Map;
import java.util.Scanner;

import static com.uh.common.constant.ConsoleConstants.*;


//import org.apache.catalina.Context;
//import org.apache.catalina.LifecycleException;
//import org.apache.catalina.connector.Connector;
//import org.apache.catalina.startup.Tomcat;
//import org.apache.catalina.valves.RemoteAddrValve;
//import org.apache.coyote.ProtocolHandler;
//import org.apache.coyote.http11.Http11NioProtocol;
//import org.apache.tomcat.util.descriptor.web.FilterDef;
//import org.apache.tomcat.util.descriptor.web.FilterMap;


/**
 * 启动程序
 *
 * @author XiaoZhangTongZhi
 */
public class ConsoleApplication {

    /**
     * 配置文件名称列表
     */
    public static final String[] CONFIG_FILES = {"application.yml", "application-datasource.yml", "application-schedule.yml"};

    public static final String[] SCAN_PACKAGES = {"com.uh"};

    public static void main(String[] args) throws Exception {

        // Default to headless mode for server runtime; allow explicit JVM override.
        if (System.getProperty("java.awt.headless") == null) {
            System.setProperty("java.awt.headless", "true");
        }

        ApplicationRootConfig rootConfig = YamlConfigManager.loadConfig(CONFIG_FILES);

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        initLogging(rootConfig.getLogging());

        if (args.length == 2 && MAIN_ARG_RESTORE.equals(args[0])) {
            //备份恢复数据，开始从备份文件中恢复数据库文件

            String fileName = args[1];
            File restoreFile = AppHomeConfig.getAbsoluteFile(AppHomeConfig.DATABASE_BACKUP_PATH, args[1]);

            System.out.println("Now begin to restore database data from:" + restoreFile);// 提示用户输入字符串
            System.out.println("Current data in database will be replaced! Please be confirm(Y/n):");
            Scanner scanner = new Scanner(System.in);// 获得控制台输入流

            String text = scanner.nextLine();// 获得用户输入
            if (text != null && text.trim().equalsIgnoreCase("Y")) {
                System.out.println("Database restoring .....");

                //这里没有启动Spring容器，直接
                DatabaseService backupService = new DatabaseServiceImpl();
                backupService.restoreDatabase(fileName);
                System.out.println("Database restoring completed.");
            } else {
                System.out.println("Quit database restoring.");
            }
        } else if (args.length >= 1 && MAIN_ARG_BACKUP.equals(args[0])) {
            //备份导出测试，开始从数据库中导出数据到备份文件

            System.out.println("Now begin to export database data into data/dbbak/");
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            initServiceContext(context);

            context.getBean(DatabaseService.class).backupDatabase();
            context.close();
            System.exit(0);

        } else {
            Logger logger = LoggerFactory.getLogger(ConsoleApplication.class);

            if (args.length >= 1 && (MAIN_ARG_INITIALIZE.equals(args[0]) || MAIN_ARG_INITIALIZE_SHORT.equals(args[0]))) {
                System.setProperty(MAIN_ENV_START_WITH_INITIALIZE, "true");
                logger.info("Console System is start with arguments '--initialize or -i'.");
            }

            try {
                DataSourceConfig dsConfig = rootConfig.getDatasource();
                MySqlDatabaseInitializer initializer = new MySqlDatabaseInitializer(
                        dsConfig.getUrl(),
                        dsConfig.getUsername(),
                        dsConfig.getPassword(),
                        rootConfig.getConsole().getAdminPasswordExpireDays());
                initializer.initializeIfAbsent();
            } catch (Throwable e) {
                logger.error("Failed to initialize database on first startup.", e);
                System.exit(1);
            }

            AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
            initWebWebappContext(context, rootConfig.getServer());  // 以TongWeb为服务器，初始化
            // initTomccatWebappContext(context, rootConfig); // 以Tomcat为服务器，初始化

        }
    }

    private static void initLogging(LoggingConfig config) {
        if (config == null || config.getLevels() == null) {
            return;
        }

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (Map.Entry<String, String> entry : config.getLevels().entrySet()) {
            String loggerName = entry.getKey();
            String levelStr = entry.getValue();
            Level level = Level.toLevel(levelStr, Level.INFO);
            if ("ROOT".equalsIgnoreCase(loggerName)) {
                loggerContext.getLogger("ROOT").setLevel(level);
            } else {
                loggerContext.getLogger(loggerName).setLevel(level);
            }
        }
        System.out.println("Logging initialized.");
    }


    private static ConfigurableApplicationContext initServiceContext(AnnotationConfigApplicationContext context) {

        SpringUtils.setApplicationContext(context);
        // 2. 自动扫描Bean
        context.scan(SCAN_PACKAGES);
        context.refresh();
        return context;
    }


    private static ConfigurableApplicationContext initWebWebappContext(AnnotationConfigWebApplicationContext applicationContext, ServerConfig serverConf) {

        String starterClass = "com.uh.common.core." + serverConf.getStarter();

        //通过反射机制创建实例, starterClass 是WebServerStarter的子类
        WebServerStarter serverStarter;
        try {
            Class<?> clazz = Class.forName(starterClass);
            serverStarter = (WebServerStarter) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create web server starter instance: " + starterClass, e);
        }

        ServletContext servletContext = serverStarter.initWebServer(serverConf);

        SpringUtils.setApplicationContext(applicationContext);
        applicationContext.setServletContext(servletContext);
        applicationContext.scan(SCAN_PACKAGES);
        applicationContext.register(ApplicationMvcConfigurer.class);
        applicationContext.refresh();

        DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext); // Configure DispatcherServlet
        serverStarter.addServlet("dispatcherServlet", dispatcherServlet, "/web-api/*");

        StaticResourceServlet staticResourceServlet = new StaticResourceServlet();
        serverStarter.addServlet("staticResourceServlet", staticResourceServlet, "/*");

        // Register XSS Filter
        XssFilter xssFilter = new XssFilter();
        xssFilter.setExcludes(FrameworkConstants.XSS_EXCLUDE_PATTERNS);
        serverStarter.addFilter("xssFilter", xssFilter, FrameworkConstants.XSS_URL_PATTERNS);

        WebConfigFilter webConfigFilter = new WebConfigFilter();
        webConfigFilter.setApplicationContext(applicationContext);
        webConfigFilter.setCorsEnabled(serverConf.getCors().isEnabled());
        webConfigFilter.setCorsAllowedOrigin(serverConf.getCors().getAllowedOrigin());
        serverStarter.addFilter("webConfigFilter", webConfigFilter, "/*");

        serverStarter.start();

        return applicationContext;
    }

}
