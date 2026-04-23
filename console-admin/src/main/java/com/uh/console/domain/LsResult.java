package com.uh.console.domain;

/**
 * 这个对象是作为文件浏览的结果返回给前端的
 */
public class LsResult {
    private String fileName;
    private boolean isDir;

    public LsResult() {
    }

    public LsResult(String fileName, boolean isDir) {
        this.fileName = fileName;
        this.isDir = isDir;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    @Override
    public String toString() {
        return "LsResult{" +
                "fileName='" + fileName + '\'' +
                ", isDir=" + isDir +
                '}';
    }
}
