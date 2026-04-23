package com.uh.common.config;

/**
 * 服务器配置
 */
public class ServerConfig {

    // 服务启动类名称
    private String starter;

    // 设置服务端口，默认值8083
    private int port = 8083;

    // 设置访问的上下文路径，默认为根路径"/"
    private String contextPath = "/";

    // 设置IP白名单，多个IP用|分隔可以使用通配符，如 "192.168.3.*|192.168.1.100|192.168.1.101"
    private String whiteList;

    //Tomcat的URI编码
    private String uriEncoding = "UTF-8";

    // 连接数满后的排队数，默认为100
    private int acceptCount = 100;

    // tomcat最大线程数，默认为200
    private int maxThreads = 200;

    // Tomcat启动初始化的线程数，默认值10
    private int minSpareThreads = 10;

    private ServerCorsConfig cors;

    private ServerSslConfig ssl;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(String whiteList) {
        this.whiteList = whiteList;
    }

    public String getUriEncoding() {
        return uriEncoding;
    }

    public void setUriEncoding(String uriEncoding) {
        this.uriEncoding = uriEncoding;
    }

    public int getAcceptCount() {
        return acceptCount;
    }

    public void setAcceptCount(int acceptCount) {
        this.acceptCount = acceptCount;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getMinSpareThreads() {
        return minSpareThreads;
    }

    public void setMinSpareThreads(int minSpareThreads) {
        this.minSpareThreads = minSpareThreads;
    }

    public ServerCorsConfig getCors() {
        return cors;
    }

    public void setCors(ServerCorsConfig cors) {
        this.cors = cors;
    }

    public ServerSslConfig getSsl() {
        return ssl;
    }

    public void setSsl(ServerSslConfig ssl) {
        this.ssl = ssl;
    }

    public String getStarter() {
        return starter;
    }

    public void setStarter(String starter) {
        this.starter = starter;
    }
}
