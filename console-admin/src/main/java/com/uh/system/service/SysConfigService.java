package com.uh.system.service;

import java.util.List;

import com.uh.system.domain.SysConfig;

/**
 * 参数配置 服务层
 *
 * @author XiaoZhangTongZhi
 */
public interface SysConfigService
{

    /**
     * 查询参数配置信息
     *
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    public SysConfig selectConfigById(Long configId);

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数key
     * @return 参数键值, 如果参数不存在返回 null
     */
    String getConfigByKey(String configKey);

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数key
     * @return 参数键值, 如果参数不存在返回 null
     */
    SysConfig getSysConfigByKey(String configKey);

    /**
     * 查询参数配置列表
     *
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    public List<SysConfig> selectConfigList(SysConfig config);

    /**
     * 新增参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    public int insertConfig(SysConfig config);

    /**
     * 修改参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    public int updateConfig(SysConfig config);

    /**
     * 根据Key更新参数配置，如果key不存在会抛出异常。
     *
     * @param config 参数SysConfig对象，属性更新指定key对应的数据行
     * @return 结果
     */
    int updateConfigByKey(SysConfig config);

    /**
     * 修改参数配置, 如果参数配置项不存在就自动插入。
     *
     * @param configKey 参数key
     * @param configValue 参数value
     * @return 结果
     */
    int setConfigValueByKey(String configKey, String configValue);

    /**
     * 通过key值删除对应的config项
     * @param key
     * @return
     */
    int deleteConfigByKey(String key);

    /**
     * 批量删除参数信息
     *
     * @param configIds 需要删除的参数ID
     */
    public void deleteConfigByIds(Long[] configIds);


    /**
     * 加载参数缓存数据
     */
    public void loadingConfigCache();

}
