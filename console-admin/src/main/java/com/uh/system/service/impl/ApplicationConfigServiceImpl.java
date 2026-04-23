package com.uh.system.service.impl;

import com.uh.common.config.ConsoleConfig;
import com.uh.common.config.UserConfig;
import com.uh.common.utils.SecurityUtils;
import com.uh.system.domain.SysConfig;
import com.uh.system.domain.SysUser;
import com.uh.system.manage.MenuRegistry;
import com.uh.system.mapper.SysUserMapper;
import com.uh.system.service.ApplicationConfigService;
import com.uh.system.service.SysConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.uh.common.constant.ConsoleConstants.*;
import static com.uh.common.constant.Constants.ADMIN_USER_IDS;

@Service
public class ApplicationConfigServiceImpl implements ApplicationConfigService {
    protected final Logger logger = LoggerFactory.getLogger("ROOT"); // 使用ROOT日志记录器

    @Resource
    private ConsoleConfig consoleConfig; // 这里通过引用bean方式，以保证ConsoleConfig的配置项被spring framework加载

    @Resource
    private SysConfigService sysConfigService;

    @Resource
    private MenuRegistry menuRegistry;

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private UserConfig userConfig;


    /**
     * 项目启动时，初始化参数到缓存
     */
    @Override
    @PostConstruct
    public void init()
    {
        boolean startWithInitialize = Boolean.parseBoolean(System.getProperty(MAIN_ENV_START_WITH_INITIALIZE));

        sysConfigService.loadingConfigCache();

        initializeSystem(startWithInitialize);

        boolean developmentMode = Boolean.parseBoolean(sysConfigService.getConfigByKey(CONFIG_SYS_DEVELOPMENT_MODE));
        switchDevelopmentMode(developmentMode);

        logger.info("{} [version={}, buildTime={}]",
                consoleConfig.getName(), consoleConfig.getVersion(), consoleConfig.getBuildTime());
    }


    @Transactional
    public synchronized boolean initializeSystem(boolean startWithInitialize) {
        boolean sysInitialized = Boolean.parseBoolean(sysConfigService.getConfigByKey(CONFIG_SYS_INITIALIZED_KEY));
        //consoleConfig.startWithInitialize 可以通过启动命令行参数 -i --initialize 来更改为true
        if(startWithInitialize || !sysInitialized) {
            logger.info("System is initializing ......");

            //重置admin user密码
            resetAdminPassword();

            //设置系统初始化标记
            sysConfigService.updateConfigByKey(new SysConfig(CONFIG_SYS_INITIALIZED_KEY, "true"));

            return true;
        }

        return false;
    }

    /**
     * 开启或关闭开发者模式，并根据模式加载对应标签的菜单
     * @param open true 开启， false 关闭
     */
    @Override
    public void switchDevelopmentMode(Boolean open) {
        List<String> menuLabels = new ArrayList<>();
        menuLabels.add("default");

        if (open) {
            logger.info("On Development Mode!");
            menuLabels.add("dev");

            sysConfigService.setConfigValueByKey(CONFIG_SYS_DEVELOPMENT_MODE, "true");
        }
        else {
            sysConfigService.setConfigValueByKey(CONFIG_SYS_DEVELOPMENT_MODE, "false");
        }

        menuRegistry.init(menuLabels);
    }

    /**
     * 重置admin的password，并设置为密码过期（登录后需要立即修改）
     */
    private void resetAdminPassword() {
        for(Long adminUserId : ADMIN_USER_IDS) {
            SysUser adminUser = userMapper.selectUserById(adminUserId);
            String initEncryptPassword = SecurityUtils.encryptPassword(userConfig.getInitAdminPassword());

            if(adminUser != null) {
                if(SecurityUtils.matchesPassword(userConfig.getInitAdminPassword(), adminUser.getPassword())) {
                    logger.warn("Administration User '{}' is still using the initial password, password reset skipped.", adminUser.getUserName());
                }
                else {
                    adminUser.setPasswordExpired(new Date(System.currentTimeMillis()));
                    adminUser.setPassword(initEncryptPassword);
                    adminUser.setLoginLocked("0");
                    adminUser.setLoginRetries(0);
                    userMapper.updateUser(adminUser);
                    logger.info("Administration User '{}' password was reset to initial value.", adminUser.getUserName());
                }
            }
        }
    }

}
