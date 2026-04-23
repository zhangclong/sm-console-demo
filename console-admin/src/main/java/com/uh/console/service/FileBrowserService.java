package com.uh.console.service;

import com.uh.system.domain.vo.FileBrowserVo;

/**
 * 用作控制台端文件浏览器</br>
 * 实现功能：可以获取文件列表、选择文件、前进后退目录
 */
public interface FileBrowserService {

    /**
     * 用来获取一个目录下的文件和文件夹信息，若获取path为null或者"/"则获取根目录下的文件和文件夹信息
     * 根目录在为 apphome/upload 目录.
     *
     * @param path
     * @return
     */
    FileBrowserVo getLs(String path, boolean moveUp);

    /**
     * 用来从服务器路径导入安装包文件，返回package信息，并移动到指定的目录下面。
     * @param path
     * @param versionId
     * @param pkgType
     */
    void importPackage(String path, Long versionId, String pkgType);


//    void uploadServerFile(String filePath, String purpose);

    String getRoot();
}
