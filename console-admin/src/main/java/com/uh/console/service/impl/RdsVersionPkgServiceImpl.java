package com.uh.console.service.impl;

import java.io.File;
import java.util.List;

import com.uh.common.config.AppHomeConfig;
import com.uh.common.utils.DateUtils;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.uh.console.mapper.RdsVersionPkgMapper;
import com.uh.console.domain.RdsVersionPkg;
import com.uh.console.service.RdsVersionPkgService;
import org.springframework.transaction.annotation.Transactional;

import static com.uh.common.config.AppHomeConfig.PACKAGE_VERSION_PATH;

/**
 * 安装包信息Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-01-12
 */
@Service
public class RdsVersionPkgServiceImpl implements RdsVersionPkgService
{
    @Resource
    private RdsVersionPkgMapper rdsVersionPkgMapper;

    /**
     * 查询安装包信息
     *
     * @param packageId 安装包信息主键
     * @return 安装包信息
     */
    @Override
    public RdsVersionPkg selectRdsVersionPkgByPackageId(Long packageId)
    {
        return rdsVersionPkgMapper.selectRdsVersionPkgByPackageId(packageId);
    }

    /**
     * 查询安装包信息列表
     *
     * @param rdsVersionPkg 安装包信息
     * @return 安装包信息
     */
    @Override
    public List<RdsVersionPkg> selectRdsVersionPkgList(RdsVersionPkg rdsVersionPkg)
    {
        return rdsVersionPkgMapper.selectRdsVersionPkgList(rdsVersionPkg);
    }


    /**
     * 新增安装包信息
     *
     * @param rdsVersionPkg 安装包信息
     * @return 结果
     */
    @Override
    public int insertRdsVersionPkg(RdsVersionPkg rdsVersionPkg)
    {
        rdsVersionPkg.setCreateTime(DateUtils.getNowDate());
        return rdsVersionPkgMapper.insertRdsVersionPkg(rdsVersionPkg);
    }

    /**
     * 修改安装包信息
     *
     * @param rdsVersionPkg 安装包信息
     * @return 结果
     */
    @Override
    public int updateRdsVersionPkg(RdsVersionPkg rdsVersionPkg)
    {
        rdsVersionPkg.setUpdateTime(DateUtils.getNowDate());
        return rdsVersionPkgMapper.updateRdsVersionPkg(rdsVersionPkg);
    }

    /**
     * 批量删除安装包信息
     *
     * @param packageIds 需要删除的安装包信息主键
     * @return 结果
     */
    @Override
    public int deleteRdsVersionPkgByPackageIds(Long[] packageIds)
    {
        int ret = 0;
        for (Long id : packageIds) {
            ret += deleteRdsVersionPkgByPackageId(id);
        }
        return ret;
    }

    /**
     * 删除安装包信息信息
     *
     * @param packageId 安装包信息主键
     * @return 结果
     */
    @Override
    public int deleteRdsVersionPkgByPackageId(Long packageId)
    {
        //删除在磁盘中关联的文件
        RdsVersionPkg pkg = rdsVersionPkgMapper.selectRdsVersionPkgByPackageId(packageId);
        File f = getPkgFile(pkg);
        if(f.exists() && f.isFile()) {
            f.delete();
        }

        return rdsVersionPkgMapper.deleteRdsVersionPkgByPackageId(packageId);
    }

    /**
     * 删除某一个版本的所有安装包
     * @param versionId
     * @return
     */
    @Override
    @Transactional
    public int deleteByVersionId(Long versionId) {
        int ret = 0;
        RdsVersionPkg param = new RdsVersionPkg();
        param.setVersionId(versionId);
        List<RdsVersionPkg> packages =  rdsVersionPkgMapper.selectRdsVersionPkgList(param);

        for (RdsVersionPkg pkg : packages) {
            ret += deleteRdsVersionPkgByPackageId(pkg.getPackageId());
        }

        return ret;
    }



    @Override
    public File getPkgFile(RdsVersionPkg pkg) {
        File dir = AppHomeConfig.getAbsoluteFile(PACKAGE_VERSION_PATH, "v" + pkg.getVersionId());
        return new File(dir, pkg.getFileName());
    }
}
