package com.uh.console.service.impl;

import com.uh.common.config.AppHomeConfig;
import com.uh.common.config.ConsoleConfig;
import com.uh.common.config.UserConfig;
import com.uh.common.exception.ServiceException;
import com.uh.common.utils.SecurityUtils;
import com.uh.common.utils.StringUtils;
import com.uh.common.utils.file.FileUtils;
import com.uh.console.domain.LsResult;
import com.uh.console.domain.RdsVersionPkg;
import com.uh.console.service.FileBrowserService;
import com.uh.console.service.RdsVersionPkgService;
import com.uh.framework.cache.ObjectCache;
import com.uh.system.domain.vo.FileBrowserVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static com.uh.common.config.AppHomeConfig.PACKAGE_VERSION_PATH;


@Service
public class FileBrowserServiceImpl implements FileBrowserService {

    private static final Logger logger = LoggerFactory.getLogger(FileBrowserServiceImpl.class);

    private final File ROOT_PATH;
    private final String ROOT_PATH_STR;

    @Autowired
    private ObjectCache cache;

    @Autowired
    private RdsVersionPkgService rdsVersionPkgService;

    @Resource
    private UserConfig userConf;

    @Resource(name = "scheduledExecutorService")
    private ScheduledExecutorService taskExecutor;

    @Resource
    private ConsoleConfig consoleConf;

    public FileBrowserServiceImpl() {
        ROOT_PATH_STR = System.getProperty("user.dir") + "/data/upload/";
        ROOT_PATH = new File(ROOT_PATH_STR);
    }

    @Override
    public FileBrowserVo getLs(String path, boolean moveUp) {
        String currentPath = ROOT_PATH_STR;

        if (!StringUtils.isEmpty(path) && !path.equals("/")) {
            currentPath = path;
        }

        List<LsResult> lsResults = new ArrayList<>();


        File scanFile = new File(currentPath);
        if (moveUp) {
            scanFile = scanFile.getParentFile();
        }
        if (!scanFile.getAbsolutePath().startsWith(ROOT_PATH.getAbsolutePath()))
            throw new ServiceException("console.filebrower.forbidmove", "apphome/data/upload/");

        List<File> listFilesAndDirs = FileUtils.getFileAndDirs(scanFile);

        for (File file : listFilesAndDirs) {
            if (file.equals(scanFile)) continue;
            lsResults.add(new LsResult(file.getName(), file.isDirectory()));
        }
        return new FileBrowserVo(lsResults, scanFile.getAbsolutePath());
    }


    @Override
    public void importPackage(String path, Long versionId, String pkgType) {
        File packageFile = new File(path);
        checkFile(packageFile);

        RdsVersionPkg paramPkg = new RdsVersionPkg();
        paramPkg.setVersionId(versionId);
        paramPkg.setPkgType(pkgType);
        List<RdsVersionPkg> resList = rdsVersionPkgService.selectRdsVersionPkgList(paramPkg);
        if (resList.size() >= 1) {
            throw new ServiceException("console.filebrower.package.repeat.error");
        }

        String destFileName = "v" + versionId + "-" + packageFile.getName();

        File file = new File(AppHomeConfig.getAbsoluteFile(PACKAGE_VERSION_PATH, "v" + versionId), destFileName);

        File parentFile = file.getParentFile();

        if (!parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                throw new ServiceException("console.filebrower.newfolder.error", file.getAbsolutePath());
            }
        }
        try {
            Files.copy(packageFile.toPath(), file.toPath());
        } catch (IOException e) {
            logger.warn("move file error,by path is {} err:", file.getAbsolutePath(), e);
            throw new ServiceException("console.filebrower.moveFile.error", e.getMessage());
        }

        RdsVersionPkg pkg = new RdsVersionPkg();
        pkg.setVersionId(versionId);
        pkg.setPkgName(file.getName().replace(".tar.gz", ""));
        pkg.setPkgType(pkgType);
        pkg.setCreateBy(SecurityUtils.getLoginUser().getUsername());
        pkg.setFileName(destFileName);
        try {
            pkg.setFileSize(Files.size(file.toPath()));
        } catch (IOException e) {
            logger.warn("get file size error,by path is {} err:", file.getAbsolutePath(), e);
            throw new ServiceException("console.filebrower.getsize.error", e.getMessage());
        }

        rdsVersionPkgService.insertRdsVersionPkg(pkg);
    }


    @Override
    public String getRoot() {
        return ROOT_PATH.getAbsolutePath();
    }

    /**
     * 检查文件是否存在并检查是否在当前指定的活动目录下面。
     *
     * @param file
     */
    private void checkFile(File file) {
        if (!file.exists()) {
            throw new ServiceException("console.filebrower.file.not.exists");
        }
        if (file.isDirectory()) {
            throw new ServiceException("console.filebrower.file.notfolder");
        }

        if (!file.getAbsolutePath().startsWith(ROOT_PATH.getAbsolutePath())) {
            throw new ServiceException("console.filebrower.only.allowed.error");
        }
    }

}
