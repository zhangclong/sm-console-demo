package com.uh.common.core;

import com.uh.common.config.ServerConfig;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.RemoteAddrValve;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import java.io.File;

public class TomcatServerStarter implements WebServerStarter {

    private Tomcat tomcat;

    private Context webContext;

    private String contextPath;

    @Override
    public ServletContext initWebServer(ServerConfig serverConfig) {
        this.tomcat = new Tomcat();

        this.contextPath = serverConfig.getContextPath();

        tomcat.setPort(serverConfig.getPort()); // Set the port number

        // 使用系统临时目录，避免在当前路径创建 tomcat 文件夹
        tomcat.setBaseDir(System.getProperty("java.io.tmpdir"));

        // Configure Tomcat connection settings
        Connector conn = tomcat.getConnector();
        conn.setURIEncoding(serverConfig.getUriEncoding());
        conn.setProperty("acceptCount", Integer.toString(serverConfig.getAcceptCount()));
        conn.setProperty("maxThreads", Integer.toString(serverConfig.getMaxThreads()));
        conn.setProperty("minSpareThreads", Integer.toString(serverConfig.getMinSpareThreads()));

        if (serverConfig.getSsl().isEnabled()) {
            conn.setSecure(true);
            conn.setScheme("https");
            conn.setProperty("SSLEnabled", "true");
            conn.setProperty("sslProtocol", "TLS");
            conn.setProperty("keyAlias", serverConfig.getSsl().getKeyAlias());
            conn.setProperty("keystoreType", serverConfig.getSsl().getKeyStoreType());
            conn.setProperty("keystoreFile", serverConfig.getSsl().getKeyStore());
            conn.setProperty("keystorePass", serverConfig.getSsl().getKeyStorePassword());
        }

        // Add web application context
        this.webContext = tomcat.addContext(contextPath, new File(".").getAbsolutePath());

        // Configure IP whitelist
        if (serverConfig.getWhiteList() != null && !serverConfig.getWhiteList().isEmpty()) {
            RemoteAddrValve remoteAddrValve = new RemoteAddrValve();
            remoteAddrValve.setAllow(serverConfig.getWhiteList());
            webContext.getPipeline().addValve(remoteAddrValve);
        }

        return webContext.getServletContext();
    }

    @Override
    public void addFilter(String filterName, Filter filter, String... urlPatterns) {
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(filterName);
        filterDef.setFilter(filter);

        this.webContext.addFilterDef(filterDef);
        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(filterName);
        for (String pattern : urlPatterns) {
            filterMap.addURLPattern(pattern);
        }

        this.webContext.addFilterMap(filterMap);
    }


    @Override
    public void addServlet(String servletName, Servlet servlet, String... urlMappings) {
        Wrapper wrapper = tomcat.addServlet(contextPath, servletName, servlet);

        for (String mapping : urlMappings) {
            wrapper.addMapping(mapping);
        }
    }


    @Override
    public void start() {
        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Console web server started!");
        tomcat.getServer().await();
    }
}
