package com.uh.system.domain.vo;

import com.uh.console.domain.LsResult;

import java.util.List;

public class FileBrowserVo {
    private List<LsResult> lsResultList;
    private String currentPath;

    public FileBrowserVo() {
    }

    public FileBrowserVo(List<LsResult> lsResultList, String currentPath) {
        this.lsResultList = lsResultList;
        this.currentPath = currentPath;
    }

    public List<LsResult> getLsResultList() {
        return lsResultList;
    }

    public void setLsResultList(List<LsResult> lsResultList) {
        this.lsResultList = lsResultList;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    @Override
    public String toString() {
        return "FileBrowserVo{" +
                "lsResultList=" + lsResultList +
                ", currentPath='" + currentPath + '\'' +
                '}';
    }
}
