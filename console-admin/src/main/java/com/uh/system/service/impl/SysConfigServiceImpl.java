package com.uh.system.service.impl;

import java.util.Collection;
import java.util.List;

import com.uh.common.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.uh.common.constant.CacheConstants;
import com.uh.common.constant.UserConstants;
import com.uh.framework.cache.ObjectCache;
import com.uh.common.core.text.Convert;
import com.uh.common.exception.ServiceException;
import com.uh.common.utils.StringUtils;
import com.uh.system.domain.SysConfig;
import com.uh.system.mapper.SysConfigMapper;
import com.uh.system.service.SysConfigService;

import static com.uh.common.utils.DateUtils.checkDateFormat;

/**
 * 参数配置 服务层实现
 *
 * @author XiaoZhangTongZhi
 */
@Service("sysConfigService")
public class SysConfigServiceImpl implements SysConfigService
{
    protected final Logger logger = LoggerFactory.getLogger(SysConfigServiceImpl.class);

    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private ObjectCache redisCache;

    /**
     * 查询参数配置信息
     *
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    @Override
    public SysConfig selectConfigById(Long configId)
    {
        SysConfig config = new SysConfig();
        config.setConfigId(configId);
        return configMapper.selectConfig(config);
    }

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数key
     * @return 参数键值, 如果参数不存在返回 null
     */
    @Override
    public String getConfigByKey(String configKey)
    {
        String configValue = Convert.toStr(redisCache.getCacheObject(getCacheKey(configKey)));
        if (StringUtils.isNotEmpty(configValue))
        {
            return configValue;
        }
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        SysConfig retConfig = configMapper.selectConfig(config);
        if (StringUtils.isNotNull(retConfig))
        {
            redisCache.setCacheObject(getCacheKey(configKey), retConfig.getConfigValue());
            return retConfig.getConfigValue();
        }

        return null;
    }

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数key
     * @return 参数键值, 如果参数不存在返回 null
     */
    @Override
    public SysConfig getSysConfigByKey(String configKey)
    {

        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        SysConfig retConfig = configMapper.selectConfig(config);
        if (StringUtils.isNotNull(retConfig))
        {
            redisCache.setCacheObject(getCacheKey(configKey), retConfig.getConfigValue());
            return retConfig;
        }

        return null;
    }

    /**
     * 查询参数配置列表
     *
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    @Override
    public List<SysConfig> selectConfigList(SysConfig config)
    {
        checkDateFormat(config.getParams(), "beginTime", "endTime");
        return configMapper.selectConfigList(config);
    }

    /**
     * 新增参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int insertConfig(SysConfig config)
    {
        config.setCreateTime(DateUtils.getNowDate());
        int row = configMapper.insertConfig(config);
        if (row > 0) {
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * 修改参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int updateConfig(SysConfig config)
    {
        config.setUpdateTime(DateUtils.getNowDate());
        int row = configMapper.updateConfig(config);
        if (row > 0) {
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * 根据Key更新参数配置，如果key不存在会抛出异常。
     *
     * @param config 参数SysConfig对象，属性更新指定key对应的数据行
     * @return 结果
     */
    @Override
    public int updateConfigByKey(SysConfig config) {
        int row = configMapper.updateConfigByKey(config);
        if (row > 0) {
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        else {
            throw new RuntimeException("Update sys_config failed! config_key='" + config.getConfigKey() + "' not be found!" );
        }
        return row;
    }



//    /**
//     * 设置多个参数配置，如果key不存在会新增。
//     *
//     * @param configs
//     * @return 更新或数量
//     */
//    @Override
//    public int setConfigs(List<SysConfig> configs) {
//        int rowCount = 0;
//        for(SysConfig config : configs) {
//            config.setUpdateTime(DateUtils.getNowDate());
//            config.setCreateTime(DateUtils.getNowDate());
//            int row = configMapper.insertUpdateConfig(config);
//            if (row > 0) {
//                rowCount += row;
//                redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
//            }
//        }
//
//        return rowCount;
//    }

    /**
     * 修改参数配置, 如果参数配置项不存在就自动插入。
     *
     * @param configKey 参数key
     * @param configValue 参数value
     * @return 结果
     */
    @Override
    public int setConfigValueByKey(String configKey, String configValue) {
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        config.setConfigValue(configValue);
        int row = configMapper.updateConfigByKey(config);

        //如果更新失败尝试插入操作
        if(row <= 0) {
            config.setConfigName(configKey);
            row = configMapper.insertConfig(config);
        }

        if (row > 0) {
            redisCache.setCacheObject(getCacheKey(configKey), configValue);
        }
        return row;
    }

    @Override
    public int deleteConfigByKey(String key) {
        int ret = configMapper.deleteConfigByKey(key);
        redisCache.deleteObject(getCacheKey(key));
        return ret;
    }

    /**
     * 批量删除参数信息
     *
     * @param configIds 需要删除的参数ID
     */
    @Override
    public void deleteConfigByIds(Long[] configIds)
    {
        for (Long configId : configIds)
        {
            SysConfig config = selectConfigById(configId);
            if (StringUtils.equals(UserConstants.YES, config.getConfigType()))
            {
                throw new ServiceException("sys.sysConfig.innerKey.no.deleted", config.getConfigKey());
            }
            configMapper.deleteConfigById(configId);
            redisCache.deleteObject(getCacheKey(config.getConfigKey()));
        }
    }

    /**
     * 加载参数缓存数据
     */
    @Override
    public void loadingConfigCache()
    {
        //删除之前在cache中的keys(如果存在）
        Collection<String> keys = redisCache.keys(CacheConstants.SYS_CONFIG_KEY + "*");
        redisCache.deleteObject(keys);

        //从数据库加载keys
        List<SysConfig> configsList = configMapper.selectConfigList(new SysConfig());
        for (SysConfig config : configsList)
        {
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    private String getCacheKey(String configKey)
    {
        return CacheConstants.SYS_CONFIG_KEY + configKey;
    }
}
