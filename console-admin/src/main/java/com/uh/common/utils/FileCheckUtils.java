package com.uh.common.utils;

import java.io.File;
import java.util.HashSet;

public class FileCheckUtils {

    private final static String[] FORBID_DIR = {"`", "|", ">", "<"};

    private final static String[]  PENETRATION_DIR = {"`", "|", ">", "<", "../", "..\\"};

    private final static String[]  PACKAGE_FILE_TYPES = {".tar.gz", ".zip", ".tgz", ".tar", ".gz"};

    /**
     * 检测是否合法
     * @param dir
     * @return
     */
    public static boolean checkAllowedDir(String dir)  {
        if(dir != null) {
            for (String f : FORBID_DIR) {
                if (dir.contains(f)) {
                    return false;
                }
            }
        }
        else {
            return false;
        }

        return true;
    }

    public static boolean checkNoPenetrationDir(String dir)  {
        if(dir != null) {
            for (String f : PENETRATION_DIR) {
                if (dir.contains(f)) {
                    return false;
                }
            }
        }
        else {
            return false;
        }

        return true;
    }

    /**
     * 判断是否是压缩包类型, 文件名后缀为 PACKAGE_FILE_TYPES = {".tar.gz", ".zip", ".tgz", ".tar", ".gz"}
     * @param fileName
     * @return
     */
    public static boolean isPackageTypeFile(String fileName) {
        if(fileName != null) {
            String lf = fileName.trim().toLowerCase();
            for (String typeSuffix : PACKAGE_FILE_TYPES) {
                if (lf.endsWith(typeSuffix)) {
                    return true;
                }
            }
        }

        return false;
    }


    public static void cleanDirectory(File parent, String... excludes) {
        HashSet<String> excludeFiles = new HashSet<>();
        if (excludes != null) {
            for (String e : excludes) {
                excludeFiles.add(e);
            }
        }

        if (parent != null && parent.exists()
                && !excludeFiles.contains(parent.getName())) {
            if (parent.isDirectory()) {
                for (File file : parent.listFiles()) {
                    String name = file.getName();
                    if (!excludeFiles.contains(name)) {
                        if (file.isDirectory()) {
                            cleanDirectory(file, excludes);
                        }
                        file.delete();
                    }
                }
            }
            parent.delete();
        }
    }

}
