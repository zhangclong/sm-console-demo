package com.uh.common.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.uh.common.config.AppHomeConfig;
import com.uh.common.config.ApplicationRootConfig;
import com.uh.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.uh.common.config.AppHomeConfig.CONFIG_PATH;

/**
 * 读取项目相关配置，配置为yaml格式
 * 此处不能使用logger输出日志，因为logger的配置也依赖于yaml配置文件中的内容。
 */
public class YamlConfigManager {

    //private final static Logger log = LoggerFactory.getLogger(YamlConfigManager.class);

    private static ApplicationRootConfig rootConfig;

    public static ApplicationRootConfig loadConfig(String[] configFiles) {
        try {
            ClassLoader loader = YamlConfigManager.class.getClassLoader();

            Yaml yaml = new Yaml();

            //从classpath中读取配置文件
            Map<String, Object> dataMap = new LinkedHashMap<>();
            for(String file : configFiles) {
                InputStream inputStream = loader.getResourceAsStream(file);
                if(inputStream != null) {
                    Map<String, Object> data = yaml.load(inputStream);
                    appendNewMap(dataMap, data);
                    //System.out.println("Loaded config from classpath: " + file);
                }
            }

            //从config目录中读取配置文件
            for(String fileName : configFiles) {
                File file = AppHomeConfig.getAbsoluteFile(CONFIG_PATH, fileName);
                if(file.exists() && file.isFile()) {
                    Map<String, Object> data = yaml.load(Files.newInputStream(file.toPath()));
                    appendNewMap(dataMap, data);
                    System.out.println("Loaded configuration from file: " + file.getAbsolutePath());
                }
            }

            convertContextPath(dataMap);

//            String mergedYamlContent = yaml.dump(dataMap);
//
//            LoaderOptions options = new LoaderOptions();
//            Yaml mergedYaml = new Yaml(new Constructor(ApplicationRootConfig.class, options));
//
//            rootConfig = mergedYaml.load(mergedYamlContent);

            // 合并后的 dataMap
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            rootConfig = mapper.convertValue(dataMap, ApplicationRootConfig.class);

            // 检测配置文件中是否有未知属性，如果有就打印警告日志
            Map<String, Object> rootConfigMap = mapper.convertValue(rootConfig, Map.class);
            logUnknownProperties(dataMap, rootConfigMap, "");


            System.out.println("Console application configuration loaded successfully.");
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }

        return rootConfig;
    }

    public static ApplicationRootConfig getRootConfig() {
        return rootConfig;
    }

    // 递归对比并打印未知属性
    private static void logUnknownProperties(Map<String, Object> data, Map<String, Object> config, String prefix) {
        for (String key : data.keySet()) {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            if (!config.containsKey(key)) {
                System.out.println("Unknown property in application config: " + fullKey);
            } else {
                Object dataVal = data.get(key);
                Object configVal = config.get(key);
                if (dataVal instanceof Map && configVal instanceof Map) {
                    logUnknownProperties((Map<String, Object>) dataVal, (Map<String, Object>) configVal, fullKey);
                }
            }
        }
    }

    /**
     * 为了保证老版本的配置文件仍然可用，做如下转换
     *  将server.servet.context-path 转换为 server.contextPath
     *  将 server.allowedOrigin 改到 server.cors.allowedOrigin
     * @param data1
     * @return 如果找到context-path则返回context-path的值，否则返回null
     */
    private static void convertContextPath(Map<String, Object> data1) {

        LinkedHashMap<String, Object> serverMap = (LinkedHashMap<String, Object>) data1.get("server");

        // 将server.servet.context-path 转换为 server.contextPath
        Object servletObj = serverMap.get("servlet");
        if(servletObj != null) {
            LinkedHashMap<String, Object> servletMap = (LinkedHashMap<String, Object>) servletObj;
            Object contextPathObj = servletMap.get("contextPath");
            serverMap.remove("servlet");
            if (contextPathObj != null && contextPathObj instanceof String) {
                serverMap.put("contextPath", contextPathObj);
                System.out.println("Converted server.servlet.context-path to server.contextPath: " + contextPathObj);
            }
        }

        // 将 server.allowedOrigin 改到 server.cors.allowedOrigin
        Object allowedOriginObj = serverMap.get("allowedOrigin");
        if(allowedOriginObj != null && allowedOriginObj instanceof String) {
            LinkedHashMap<String, Object> corsMap = (LinkedHashMap<String, Object>) serverMap.get("cors");
            if(corsMap == null) {
                corsMap = new LinkedHashMap<String, Object>();
                serverMap.put("cors", corsMap);
            }
            corsMap.put("allowedOrigin", allowedOriginObj);
            serverMap.remove("allowedOrigin");
            System.out.println("Moved server.allowedOrigin to server.cors.allowedOrigin: " + allowedOriginObj);
        }

    }


    /**
     * 递归合并两个Map, 如果key的格式是 xx-xx 则转换为驼峰命名法 xxXx
     * @param existingMap  已有的Map
     * @param newMap 新的Map
     */
    private static void appendNewMap(Map<String,Object> existingMap, Map<String,Object> newMap){
        if(newMap != null) {
            newMap.forEach((key1, value) -> {
                String keyCamel = StringUtils.convertToCamelCase(key1, "-", false); // 将key转换为驼峰命名, 首字母小写
                if (value instanceof LinkedHashMap) {
                    LinkedHashMap<String, Object> newMapValue = (LinkedHashMap<String, Object>) value;
                    LinkedHashMap<String, Object> existingMapValue = (LinkedHashMap<String, Object>) existingMap.get(keyCamel);
                    if (existingMapValue == null) {
                        existingMapValue = new LinkedHashMap<String, Object>();
                        existingMap.put(keyCamel, existingMapValue);
                    }

                    appendNewMap(existingMapValue, newMapValue);
                }
                else if(value instanceof String) {
                    String val = ((String) value).trim();
                    if(val.startsWith("${") && val.endsWith("}")) {
                        // 匹配 ${ENV_NAME:defaultValue} 或 ${ENV_NAME}
                        String inner = val.substring(2, val.length() - 1);
                        String envName;
                        String defaultValue = null;
                        int colonIdx = inner.indexOf(':');
                        if (colonIdx >= 0) {
                            envName = inner.substring(0, colonIdx);
                            defaultValue = inner.substring(colonIdx + 1);
                        } else {
                            envName = inner;
                        }
                        String envValue = System.getenv(envName);
                        String resolvedValue = (envValue != null) ? envValue : defaultValue;

                        // 用yaml.load自动类型转换
                        Object resolvedObj = null;
                        if (resolvedValue != null) {
                            Yaml yamlParser = new Yaml();
                            resolvedObj = yamlParser.load(resolvedValue);
                        }

                        existingMap.put(keyCamel, resolvedValue);
                    }
                    else {
                        existingMap.put(keyCamel, value);
                    }

                }
                else {
                    existingMap.put(keyCamel, value);
                }
            });
        }
    }

}
