package com.uh.common.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;



public class ClassResourceUtils {


    /**
     * 判断classpath中是否存在指定的资源文件
     * @param resourceName
     * @return true:存在，false:不存在
     */
    public static boolean isResourceExist(String resourceName) {
        URL resourceUrl = getClassLoader().getResource(resourceName);
        return resourceUrl != null;
    }

    public static String loadResource(String resourceName) {
        return loadResource(resourceName, "UTF-8");
    }

    /**
     * 读取classpath中的资源文件的内容，返回为String类型。
     * @param resourceName
     * @return
     */
    public static String loadResource(String resourceName, String charsetName)  {
        String content = null;
        try(InputStream licenseIn = getClassLoader().getResourceAsStream(resourceName)) {
            content = IOUtils.toString(licenseIn, charsetName);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return content;
    }

    public static InputStream loadResourceAsStream(String resourceName) {
        return getClassLoader().getResourceAsStream(resourceName);
    }


    private static ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if(cl == null) {
            cl = ClassResourceUtils.class.getClassLoader();
        }
        return cl;
    }


}
