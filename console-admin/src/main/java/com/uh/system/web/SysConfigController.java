package com.uh.system.web;

import com.alibaba.fastjson2.JSON;
import com.uh.common.annotation.Log;
import com.uh.common.annotation.PrePermission;
import com.uh.common.config.ConsoleConfig;
import com.uh.common.config.UserConfig;
import com.uh.common.core.controller.BaseController;
import com.uh.common.core.domain.AjaxResult;
import com.uh.common.core.domain.LoginBody;
import com.uh.common.core.page.TableDataInfo;
import com.uh.common.enums.BusinessTypeConstants;
import com.uh.common.utils.MessageUtils;
import com.uh.common.utils.StringUtils;
import com.uh.common.utils.poi.ExcelUtil;
import com.uh.system.domain.SysConfig;
import com.uh.system.domain.SysUserPwdStrength;
import com.uh.system.service.ApplicationConfigService;
import com.uh.system.service.SysConfigService;
import com.uh.system.service.SysLoginService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参数配置 信息操作处理
 *
 * @author XiaoZhangTongZhi
 */
@RestController
@RequestMapping("/system/config")
public class SysConfigController extends BaseController {


    private static final String SYS_USER_PASSWORD_STRENGTH_KEY = "sys.user.password.strength";

    @Resource
    private ApplicationConfigService applicationConfigService;

    @Resource
    private SysConfigService sysConfigService;

    @Resource
    private SysLoginService sysLoginService;

    @Resource
    private ConsoleConfig consoleConf;

    @Resource
    private UserConfig userConfig;

    /**
     * 获取参数配置列表
     */
    @PrePermission("system:config:list")
    @GetMapping("/list")
    public TableDataInfo list(SysConfig config) {
        startPage();
        List<SysConfig> list = sysConfigService.selectConfigList(config);
        return getDataTable(list);
    }

    @Log(title = "ParameterManagement", businessType = BusinessTypeConstants.EXPORT)
    @PrePermission("system:config:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysConfig config) {
        List<SysConfig> list = sysConfigService.selectConfigList(config);
        ExcelUtil<SysConfig> util = new ExcelUtil<SysConfig>(SysConfig.class);
        util.exportExcel(response, list, "参数数据");
    }

    /**
     * 根据参数编号获取详细信息
     */
    @PrePermission("system:config:query")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable("configId") Long configId) {
        return AjaxResult.success(sysConfigService.selectConfigById(configId));
    }

    /**
     * 根据参数键名查询参数值
     */
    @GetMapping(value = "/configKey/{configKey}")
    public AjaxResult getConfigKey(@PathVariable("configKey") String configKey) {
        return AjaxResult.success(sysConfigService.getConfigByKey(configKey));
    }


    /**
     * 返回系统对外显示的关键配置信息（application.yml中定义）
     */
    @GetMapping(value = "/appConfigKey/{key}")
    public AjaxResult getAppConfigKey(@PathVariable("key") String key) {
        if ("console.name".equals(key)) {
            return AjaxResult.success("", MessageUtils.message("console.name"));
        } else if ("console.version".equals(key)) {
            return AjaxResult.success("", getVersion());
        } else if ("console.copyrightYear".equals(key)) {
            return AjaxResult.success("", consoleConf.getCopyrightYear());
        } else if ("user.initPassword".equals(key)) {
            return AjaxResult.success("", userConfig.getInitPassword());
        } else {
            return AjaxResult.error("Failed to find application config key:" + key);
        }
    }

    /**
     * 返回系统对外显示的关键配置信息（application.yml中定义）， 把多个值一次性返回到Map对象中
     */
    @GetMapping(value = "/appConfigKeys")
    public AjaxResult getAppConfigKeys() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", MessageUtils.message("console.name"));
        result.put("version", getVersion());
        result.put("copyrightYear", consoleConf.getCopyrightYear());
        result.put("locale", consoleConf.getLocale());


        SysConfig pwdStrength = sysConfigService.getSysConfigByKey(SYS_USER_PASSWORD_STRENGTH_KEY);
        if (pwdStrength == null || StringUtils.isEmpty(pwdStrength.getConfigKey())) {
            return AjaxResult.success(result);
        }
        SysUserPwdStrength sysUserPwdStrength = JSON.parseObject(pwdStrength.getConfigValue(), SysUserPwdStrength.class);
        result.put("pwdStrength", sysUserPwdStrength);


        return AjaxResult.success(result);
    }


    /**
     * 新增参数配置
     */
    @PrePermission("system:config:add")
    @Log(title = "ParameterManagement", businessType = BusinessTypeConstants.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysConfig config) {
        config.setCreateBy(getUsername());
        return toAjax(sysConfigService.insertConfig(config));
    }

    /**
     * 修改参数配置
     */
    @PrePermission("system:config:edit")
    @Log(title = "ParameterManagement", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@Validated @RequestBody SysConfig config) {
        config.setUpdateBy(getUsername());
        return toAjax(sysConfigService.updateConfig(config));
    }

    /**
     * 删除参数配置
     */
    @PrePermission("system:config:remove")
    @Log(title = "ParameterManagement", businessType = BusinessTypeConstants.DELETE)
    @GetMapping("/delete/{configIds}")
    public AjaxResult remove(@PathVariable("configIds") Long[] configIds) {
        sysConfigService.deleteConfigByIds(configIds);
        return success();
    }

    /**
     * 刷新参数缓存
     */
    @PrePermission("system:config:remove")
    @Log(title = "ParameterManagement", businessType = BusinessTypeConstants.CLEAN)
    @GetMapping("/refreshCache")
    public AjaxResult refreshCache() {
        sysConfigService.loadingConfigCache();
        return AjaxResult.success();
    }

    @PostMapping("/switchondev")
    public AjaxResult switchOnDev(@RequestBody LoginBody loginBody) {
        sysLoginService.checkUserPassword(loginBody.getUsername(), loginBody.getPassword());
        applicationConfigService.switchDevelopmentMode(true);
        return AjaxResult.success();
    }

    @PostMapping("/switchoffdev")
    public AjaxResult switchOffDev(@RequestBody LoginBody loginBody) {
        sysLoginService.checkUserPassword(loginBody.getUsername(), loginBody.getPassword());
        applicationConfigService.switchDevelopmentMode(false);
        return AjaxResult.success();
    }


    @PostMapping("/userPwdStrength")
    public AjaxResult setUserPwdStrength(@RequestBody SysUserPwdStrength sysUserPwdStrength) {
        if (sysUserPwdStrength == null)
            return AjaxResult.error(MessageUtils.message("console.global.error.args.error"));

        sysConfigService.setConfigValueByKey(SYS_USER_PASSWORD_STRENGTH_KEY, JSON.toJSONString(sysUserPwdStrength));
        return AjaxResult.success();
    }


    private String getVersion() {
        return consoleConf.getVersion();
    }


}
