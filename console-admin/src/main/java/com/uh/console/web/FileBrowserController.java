package com.uh.console.web;

import com.uh.common.core.domain.AjaxResult;
import com.uh.common.utils.MessageUtils;
import com.uh.common.utils.StringUtils;
import com.uh.console.service.FileBrowserService;
import com.uh.system.domain.vo.FileBrowserVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


//@Anonymous
@RestController
@RequestMapping("/fileBrowser")
public class FileBrowserController {
    @Resource
    private FileBrowserService fileBrowserService;

    @RequestMapping("/ls")
    public AjaxResult ls(
            @RequestParam(value = "path", required = false) String path,
            @RequestParam(value = "moveUp", required = false) boolean moveUp
    ) {
        FileBrowserVo resultList = fileBrowserService.getLs(path, moveUp);
        return AjaxResult.success(resultList.getLsResultList())
                .put("currentPath", resultList.getCurrentPath())
                .put("root", fileBrowserService.getRoot());
    }

    @PostMapping("/importLicense")
    public AjaxResult importLicense(
            @RequestParam("licenseFile") String licenseFile,
            @RequestParam("licenseId") Long licenseId,
            @RequestParam("section") String section
    ) {

        throw new UnsupportedOperationException("暂不支持导入license文件");
    }

    @PostMapping("/importPackage")
    public AjaxResult upload(
            @RequestParam("file") String file,
            @RequestParam("versionId") Long versionId,
            @RequestParam("pkgType") String pkgType
    ) {
        if (StringUtils.isEmpty(file) || versionId == null || StringUtils.isEmpty(pkgType)) {
            return AjaxResult.error(MessageUtils.message("console.global.error.args.error"));
        }
        fileBrowserService.importPackage(file, versionId, pkgType);
        return AjaxResult.success();
    }

    @PostMapping("/importBatchExcel")
    public AjaxResult importBatchExcel(
            @RequestParam("file") String file,
            @RequestParam("type") String type
    ) {
//        if (StringUtils.isEmpty(file) ||  StringUtils.isEmpty(type)) {
//            return AjaxResult.error(MessageUtils.message("console.global.error.args.error"));
//        }
//        fileBrowserService.importBatchExcel(file, type);
//        return AjaxResult.success();
        throw new UnsupportedOperationException("暂不支持导入批量安装Excel文件");
    }
}
