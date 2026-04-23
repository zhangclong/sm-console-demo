package com.uh.console.service.impl;

import com.uh.common.exception.ServiceException;
import com.uh.common.utils.DateUtils;
import com.uh.console.domain.RdsVersion;
import com.uh.console.domain.RdsVersionPkg;
import com.uh.console.domain.TemplateGroupVersion;
import com.uh.console.mapper.RdsVersionMapper;
import com.uh.console.mapper.TemplateGroupVersionMapper;
import com.uh.console.service.RdsVersionPkgService;
import com.uh.console.service.RdsVersionService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 版本信息Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-01-12
 */
@Service
public class RdsVersionServiceImpl implements RdsVersionService {
    @Resource
    private RdsVersionMapper rdsVersionMapper;

    @Resource
    private TemplateGroupVersionMapper groupVersionMapper;

    @Resource
    private RdsVersionPkgService rdsVersionPkgService;

    /**
     * 查询版本信息
     *
     * @param versionId 版本信息主键
     * @return 版本信息
     */
    @Override
    public RdsVersion selectRdsVersionByVersionId(Long versionId) {
        return rdsVersionMapper.selectRdsVersionByVersionId(versionId);
    }

    @Override
    public List<RdsVersion> selectListByStatus(String status) {
        return rdsVersionMapper.selectListByStatus(status);
    }

    public RdsVersion selectDefaultVersion() {
        return rdsVersionMapper.selectDefaultVersion();
    }

    /**
     * 查询版本信息列表
     *
     * @param rdsVersion 版本信息
     * @return 版本信息
     */
    @Override
    public List<RdsVersion> selectRdsVersionList(RdsVersion rdsVersion) {
        return rdsVersionMapper.selectRdsVersionList(rdsVersion);
    }

    /**
     * 查询版本数量
     *
     * @param rdsVersion 查询条件，softwareName, versionName, status 三个条件可用。
     * @return 结果
     */
    @Override
    public Integer getVersionsCount(RdsVersion rdsVersion) {
        return rdsVersionMapper.getVersionsCount(rdsVersion);
    }

    /**
     * 新增版本信息
     *
     * @param rdsVersion 版本信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertRdsVersion(RdsVersion rdsVersion) {
        if (rdsVersion.isDefaultVersion()) {
            //新加默认版本，首先把原默认版本设置为非。
            rdsVersionMapper.updateToNoDefaultVersion(rdsVersion.getCreateBy());
        }

        rdsVersion.setCreateTime(DateUtils.getNowDate());

        try {
            int ret = rdsVersionMapper.insertRdsVersion(rdsVersion);
            Long groupId = rdsVersion.getDefaultGroupId();
            if(groupId != null && groupId > 0 && rdsVersion.getVersionId() != null) {
                TemplateGroupVersion groupVersion = new TemplateGroupVersion(groupId, rdsVersion.getVersionId());
                groupVersionMapper.insertGroupVersion(groupVersion);
            }

            return ret;
        } catch (DuplicateKeyException de) {
            throw new ServiceException("console.rdsVersion.insert.duplicate");
        }
    }

    /**
     * 修改版本信息
     *
     * @param rdsVersion 版本信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateRdsVersion(RdsVersion rdsVersion) {
        RdsVersion orgVer = rdsVersionMapper.selectRdsVersionByVersionId(rdsVersion.getVersionId());
        if (rdsVersion.isDefaultVersion() != orgVer.isDefaultVersion()) {
            if (orgVer.isDefaultVersion() == true) {
                throw new ServiceException("console.rdsVersion.disable.default.notAllowed");
            } else {
                //修改非默认版本》默认版本，首先把原默认版本设置为非。
                rdsVersionMapper.updateToNoDefaultVersion(rdsVersion.getUpdateBy());
            }
        }

        rdsVersion.setUpdateTime(DateUtils.getNowDate());
        return rdsVersionMapper.updateRdsVersion(rdsVersion);
    }

    /**
     * 批量删除版本信息
     *
     * @param versionIds 需要删除的版本信息主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteVersionBy(Long[] versionIds) {
        int count = 0;
        if (versionIds != null) {
            for (Long vid : versionIds) {
                count += deleteVersionBy(vid);
            }
        }

        return count;
    }

    /**
     * 删除版本信息信息
     *
     * @param versionId 版本信息主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteVersionBy(Long versionId) {
        RdsVersion version = rdsVersionMapper.selectRdsVersionByVersionId(versionId);
        if(version.isDefaultVersion()) {
            throw new ServiceException("console.rdsVersion.delete.default.notAllowed");
        }

        groupVersionMapper.deleteByVersionId(versionId); //删除模版组和版本的关联关系
        rdsVersionPkgService.deleteByVersionId(versionId); //删除版本对应的安装包
        return rdsVersionMapper.deleteRdsVersionByVersionId(versionId);
    }

    @Override
    @Transactional
    public int updateStatus(RdsVersion rdsVersion) {
        RdsVersion orgVer = rdsVersionMapper.selectRdsVersionByVersionId(rdsVersion.getVersionId());
        if (orgVer.isDefaultVersion() == true && "0".equals(rdsVersion.getStatus())) {
            throw new ServiceException("console.rdsVersion.disable.default.notAllowed");
        } else {
            rdsVersion.setUpdateTime(DateUtils.getNowDate());
            return rdsVersionMapper.updateStatus(rdsVersion);
        }
    }

    @Override
    @Transactional
    public int updateDefaultVersion(Long versionId, String updateUsername) {
        //找到所有 defaultVersion = true 的记录，全部变更为 defaultVersiion = false
        rdsVersionMapper.updateToNoDefaultVersion(updateUsername);

        //设置新的 defaultVersiion
        RdsVersion rdsVersion = new RdsVersion();
        rdsVersion.setVersionId(versionId);
        rdsVersion.setDefaultVersion(true);
        rdsVersion.setStatus("1");
        rdsVersion.setUpdateTime(DateUtils.getNowDate());
        return rdsVersionMapper.updateDefaultVersion(rdsVersion);
    }

    @Override
    public List<RdsVersion> selectVersionByPkgType(String pkgType) {
        RdsVersionPkg rdsVersionPkgQuery = new RdsVersionPkg();
        rdsVersionPkgQuery.setPkgType(pkgType);

        List<RdsVersionPkg> rdsVersionPkgs = rdsVersionPkgService.selectRdsVersionPkgList(rdsVersionPkgQuery);
        if (rdsVersionPkgs == null)throw new ServiceException("console.rds.version.not.exists");

        return rdsVersionMapper.selectRdsVersionByVersionIds(rdsVersionPkgs.stream().
                collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(RdsVersionPkg::getVersionId))),
                        ArrayList::new)).stream().map(RdsVersionPkg::getVersionId).toArray(Long[]::new));
    }


}
