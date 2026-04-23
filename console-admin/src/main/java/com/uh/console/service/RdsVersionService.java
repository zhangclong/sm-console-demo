package com.uh.console.service;

import java.util.List;
import com.uh.console.domain.RdsVersion;

/**
 * 版本信息Service接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-12
 */
public interface RdsVersionService
{
    /**
     * 查询版本信息
     *
     * @param versionId 版本信息主键
     * @return 版本信息
     */
    public RdsVersion selectRdsVersionByVersionId(Long versionId);

    /**
     * 查询版本数量
     * @param rdsVersion 查询条件，softwareName, versionName, status 三个条件可用。
     * @return
     */
    public Integer getVersionsCount(RdsVersion rdsVersion);

    /**
     * 查询所有版本，by status
     * @param status  "1" 启用状态，"0" 停用状态， null 查询所有版本
     * @return
     */
    List<RdsVersion> selectListByStatus(String status);

    /**
     * 获得默认的 version
     * @return
     */
    RdsVersion selectDefaultVersion();

    /**
     * 查询版本信息列表
     *
     * @param rdsVersion 版本信息
     * @return 版本信息集合
     */
    public List<RdsVersion> selectRdsVersionList(RdsVersion rdsVersion);

    /**
     * 新增版本信息
     *
     * @param rdsVersion 版本信息
     * @return 结果
     */
    public int insertRdsVersion(RdsVersion rdsVersion);

    /**
     * 修改版本信息
     *
     * @param rdsVersion 版本信息
     * @return 结果
     */
    public int updateRdsVersion(RdsVersion rdsVersion);

    /**
     * 批量删除版本信息
     *
     * @param versionIds 需要删除的版本信息主键集合
     * @return 结果
     */
    public int deleteVersionBy(Long[] versionIds);

    /**
     * 删除版本信息信息
     *
     * @param versionId 版本信息主键
     * @return 结果
     */
    public int deleteVersionBy(Long versionId);

    /**
     * 更新对应版本的状态
     * @param rdsVersion
     * @return
     */
    public int updateStatus(RdsVersion rdsVersion);

    /**
     * 设置某一个版本为默认版本
     * @param versionId
     * @return
     */
    public int updateDefaultVersion(Long versionId, String updateUsername);

    /**
     * 根据pkgType查询出Version列表
     * @param pkgType
     * @return
     */
    List<RdsVersion> selectVersionByPkgType(String pkgType);
}
