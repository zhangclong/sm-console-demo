package com.uh.common.core;

import com.uh.common.config.ServerConfig;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;


/**
 * Web服务器启动器接口
 */
public interface WebServerStarter {

    /**
     * 初始化Web服务器
     * @param serverConfig
     * @return
     */
    ServletContext initWebServer(ServerConfig serverConfig);

    /**
     * 添加过滤器
     * @param filterName
     * @param filter
     * @param urlPatterns
     */
    void addFilter(String filterName, Filter filter, String... urlPatterns);

    /**
     * 添加Servlet
     * @param servletName
     * @param servlet
     * @param urlMappings
     */
    void addServlet(String servletName, Servlet servlet, String... urlMappings);

    /**
     * 启动Web服务器
     */
    void start();
}
