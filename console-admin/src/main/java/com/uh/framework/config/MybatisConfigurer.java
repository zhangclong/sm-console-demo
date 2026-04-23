package com.uh.framework.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.uh.common.config.DataSourceConfig;
import com.uh.common.utils.spring.SpringUtils;
import com.uh.framework.dbproxy.DataSourceProxy;
import com.uh.framework.dbproxy.StatementOperationManager;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.uh.common.constant.FrameworkConstants.*;


@MapperScan("com.uh.**.mapper")// 指定要扫描的Mapper类的包的路径
@Configuration
public class MybatisConfigurer {



    @Resource
    private DataSourceConfig dataSourceConf;

    @Bean
    public DataSource dataSource() throws Exception {

        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(dataSourceConf.getDriverClassName());
        dataSource.setJdbcUrl(dataSourceConf.getUrl());
        dataSource.setUser(dataSourceConf.getUsername());
        dataSource.setPassword(dataSourceConf.getPassword());
        dataSource.setInitialPoolSize(dataSourceConf.getInitialPoolSize());
        dataSource.setMinPoolSize(dataSourceConf.getMinPoolSize());
        dataSource.setMaxPoolSize(dataSourceConf.getMaxPoolSize());
        dataSource.setAcquireRetryAttempts(dataSourceConf.getAcquireRetryAttempts());
        dataSource.setMaxIdleTime(dataSourceConf.getMaxIdleTime());
        dataSource.setMaxStatements(dataSourceConf.getMaxStatements());
        dataSource.setAcquireIncrement(dataSourceConf.getAcquireIncrement());
        dataSource.setIdleConnectionTestPeriod(dataSourceConf.getIdleConnectionTestPeriod());
        // MySQL 长连接存活检测：仅依赖周期性测试（idleConnectionTestPeriod），不在 checkin/checkout 时测试
        dataSource.setPreferredTestQuery("SELECT 1");
        dataSource.setTestConnectionOnCheckin(false);
        dataSource.setTestConnectionOnCheckout(false);
        // 获取连接超时 30 秒
        dataSource.setCheckoutTimeout(30_000);

        // 启用数据库操作代理机制
        // 会记录所有的写操作SQL语句及其参数，用于主从数据库同步；目前把SQL执行记录到日志文件(db-proxy.log)中
        StatementOperationManager.getInstance().setActiveProxy(false); // 启用代理机制
        return new DataSourceProxy(dataSource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactoryBean() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setTypeAliasesPackage(MYBATIS_TYPE_ALIASES_PACKAGE);
        factoryBean.setMapperLocations(resolveMapperLocations(split(MYBATIS_MAPPERS)));
        factoryBean.setConfigLocation(new DefaultResourceLoader().getResource(MYBATIS_CONFIG));

        // 可根据实际情况设置DataSource
        factoryBean.setDataSource(SpringUtils.getBean(DataSource.class));
        return factoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager txManager = new DataSourceTransactionManager(SpringUtils.getBean(DataSource.class));
        return txManager;
    }

    /**
     * 事务。设置全局的事务隔离级别
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager manager) {
        TransactionTemplate template = new TransactionTemplate(manager);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return template;
    }

    @Bean
    public TransactionManagementConfigurer transactionManagementConfigurer() {
        return new TransactionManagementConfigurer(){
            @Override
            public PlatformTransactionManager annotationDrivenTransactionManager() {
                return transactionManager();
            }
        };
    }

    private static org.springframework.core.io.Resource[] resolveMapperLocations(String[] mapperLocations) {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<org.springframework.core.io.Resource> resources = new ArrayList<org.springframework.core.io.Resource>();
        if (mapperLocations != null) {
            for (String mapperLocation : mapperLocations) {
                try {
                    org.springframework.core.io.Resource[] mappers = resourceResolver.getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }
        return resources.toArray(new org.springframework.core.io.Resource[resources.size()]);
    }

    /**
     * 逗号分隔字符串转换为数组
     * @param source
     * @return
     */
    private static String[] split(String source) {
        if (source == null) {
            return  new String[0];
        }
        else {
            String trimmed = source.trim();
            if (trimmed.length() == 0) {
                return new String[0];
            }

            return trimmed.split(",");
        }
    }


}
