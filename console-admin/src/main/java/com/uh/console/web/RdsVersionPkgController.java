package com.uh.console.web;

import java.io.File;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.uh.common.config.AppHomeConfig;
import com.uh.common.exception.UserBaseException;
import com.uh.common.utils.MessageUtils;
import com.uh.common.utils.file.FileUploadUtils;
import com.uh.common.utils.file.FileUtils;
import com.uh.common.utils.file.MimeTypeUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;
import com.uh.common.annotation.PrePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.uh.common.annotation.Log;
import com.uh.common.core.controller.BaseController;
import com.uh.common.core.domain.AjaxResult;
import com.uh.common.enums.BusinessTypeConstants;
import com.uh.console.domain.RdsVersionPkg;
import com.uh.console.service.RdsVersionPkgService;
import com.uh.common.utils.poi.ExcelUtil;
import com.uh.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

import static com.uh.common.config.AppHomeConfig.PACKAGE_VERSION_PATH;

/**
 * 安装包信息Controller
 *
 * @author Zhang ChenLong
 * @date 2023-01-12
 */
@RestController
@RequestMapping("/console/rdsversionpkg")
public class RdsVersionPkgController extends BaseController
{
    @Autowired
    private RdsVersionPkgService rdsVersionPkgService;

    /**
     * 查询安装包信息列表
     */
    @PrePermission("console:rdsversion:list")
    @GetMapping("/list")
    public TableDataInfo list(RdsVersionPkg rdsVersionPkg)
    {
        startPage();
        List<RdsVersionPkg> list = rdsVersionPkgService.selectRdsVersionPkgList(rdsVersionPkg);
        return getDataTable(list);
    }


    @PrePermission("console:rdsversion:add")
    @Log(title = "console:rdsversion", businessType = BusinessTypeConstants.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file, @RequestParam("versionId") Long versionId, @RequestParam("pkgType") String pkgType) throws Exception
    {
        if(versionId == null || pkgType == null) {
            throw new UserBaseException("Parameter Error for RdsVersionPkg upload!");
        }

        RdsVersionPkg paramPkg = new RdsVersionPkg();
        paramPkg.setVersionId(versionId);
        paramPkg.setPkgType(pkgType);
        List<RdsVersionPkg> resList = rdsVersionPkgService.selectRdsVersionPkgList(paramPkg);
        if(resList.size() >= 1) {
            return AjaxResult.error(MessageUtils.message("console.pkg.upload.error.repeat"));
        }

        String destFileName = "v" + versionId + "-" + FilenameUtils.getName(file.getOriginalFilename());

        String fileName = FileUploadUtils.uploadToDir(AppHomeConfig.getAbsoluteFile(PACKAGE_VERSION_PATH, "v" + versionId), destFileName, file, MimeTypeUtils.GZIP_EXTENSION);

        RdsVersionPkg pkg = new RdsVersionPkg();
        pkg.setVersionId(versionId);
        pkg.setPkgName(getName(fileName));
        pkg.setPkgType(pkgType);
        pkg.setCreateBy(getUsername());
        pkg.setFileName(fileName);
        pkg.setFileSize(file.getSize());

        rdsVersionPkgService.insertRdsVersionPkg(pkg);

        return AjaxResult.success(MessageUtils.message("console.pkg.upload.error.success"));
    }

    @PrePermission("console:rdsversion:query")
    @PostMapping(value = "/download/{packageId}")
    public void download(HttpServletResponse response, @PathVariable("packageId") Long packageId)
    {
        try
        {

            RdsVersionPkg pkg = rdsVersionPkgService.selectRdsVersionPkgByPackageId(packageId);
            File downloadFile = rdsVersionPkgService.getPkgFile(pkg);

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, pkg.getFileName());
            FileUtils.writeBytes(downloadFile, response.getOutputStream());
        }
        catch (Exception e)
        {
            logger.error("In RdsVersionPkgController.download() download error! ", e);
        }
    }



    /**
     * 导出安装包信息列表
     */
    @PrePermission("console:rdsversion:export")
    @Log(title = "console:rdsversion", businessType = BusinessTypeConstants.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RdsVersionPkg rdsVersionPkg)
    {
        List<RdsVersionPkg> list = rdsVersionPkgService.selectRdsVersionPkgList(rdsVersionPkg);
        ExcelUtil<RdsVersionPkg> util = new ExcelUtil<RdsVersionPkg>(RdsVersionPkg.class);
        util.exportExcel(response, list, "安装包信息数据");
    }

    /**
     * 获取安装包信息详细信息
     */
    @PrePermission("console:rdsversion:query")
    @GetMapping(value = "/{packageId}")
    public AjaxResult getInfo(@PathVariable("packageId") Long packageId)
    {
        return AjaxResult.success(rdsVersionPkgService.selectRdsVersionPkgByPackageId(packageId));
    }

    /**
     * 新增安装包信息
     */
    @PrePermission("console:rdsversion:add")
    @Log(title = "console:rdsversion", businessType = BusinessTypeConstants.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RdsVersionPkg rdsVersionPkg)
    {
        return toAjax(rdsVersionPkgService.insertRdsVersionPkg(rdsVersionPkg));
    }

    /**
     * 修改安装包信息
     */
    @PrePermission("console:rdsversion:edit")
    @Log(title = "console:rdsversion", businessType = BusinessTypeConstants.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody RdsVersionPkg rdsVersionPkg)
    {
        return toAjax(rdsVersionPkgService.updateRdsVersionPkg(rdsVersionPkg));
    }

    /**
     * 删除安装包信息
     */
    @PrePermission("console:rdsversion:remove")
    @Log(title = "console:rdsversion", businessType = BusinessTypeConstants.DELETE)
	@GetMapping("/delete/{packageIds}")
    public AjaxResult remove(@PathVariable("packageIds") Long[] packageIds)
    {
        return toAjax(rdsVersionPkgService.deleteRdsVersionPkgByPackageIds(packageIds));
    }

    public static String getName(String fileName) {
        String fname = fileName.trim();
        int end = fname.indexOf(".tar.gz");
        if(end <= 0) {
            end = fname.indexOf(".zip");
        }
        if(end <= 0) {
            end = fname.indexOf(".tgz");
        }

        if(end > 0) {
            return fname.substring(0, end);
        }
        else {
            return fname;
        }
    }
}
