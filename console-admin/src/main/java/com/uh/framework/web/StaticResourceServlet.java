package com.uh.framework.web;

import com.uh.common.utils.AntPathUtils;
import com.uh.common.utils.StringUtils;
import com.uh.common.utils.operation.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理静态资源请求的Servlet。
 * 这个Servlet会处理一些静态资源的请求: index.html、favicon、static/* 等。
 *
 *
 *
 * @author Zhang Chenlong
 */
public class StaticResourceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String RESOURCE_BASE = "/public";   // classpath 的资源目录

    public static final String[] INDEX_MAPPINGS =   {"/", "/index", "/404", "/login", "/system/**", "/monitor/**", "/rds/**", "/log/**"};  // 需要映射到主页面的路径

    private final static String PATH_TOBE_REPLACE = "/uhrds"; // 需要被替换为contextPath的路径片段

    private static final Map<String, String> CONTENT_TYPES = new HashMap<>();

    static {
        CONTENT_TYPES.put(".png", "image/png");
        CONTENT_TYPES.put(".gif", "image/gif");
        CONTENT_TYPES.put(".svg", "image/svg+xml");
        CONTENT_TYPES.put(".jpg", "image/jpeg");
        CONTENT_TYPES.put(".ico", "image/x-icon");
        CONTENT_TYPES.put(".js", "application/javascript;charset=UTF-8");
        CONTENT_TYPES.put(".html", "text/html;charset=UTF-8");
        CONTENT_TYPES.put(".css", "text/css;charset=UTF-8");
        CONTENT_TYPES.put(".woff", "font/woff");
        CONTENT_TYPES.put(".ttf", "font/ttf");
    }



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String target = req.getRequestURI();
        String contextPath = req.getContextPath();

        //去除其中的contextPath部分
        if( StringUtils.isNotEmpty(contextPath) && target.startsWith(contextPath)) {
            target = target.substring(contextPath.length());
        }

        // 因为是单页面应用，需要把指定路径映射到主页面 /index.html
        if(StringUtils.isEmpty(target) || AntPathUtils.matches(target, INDEX_MAPPINGS)) {
            target = "/index.html";
        }

        //System.out.println("resource target: " + target);

        try (InputStream resourceStream = getClass().getResourceAsStream(RESOURCE_BASE + target)) {
            if (resourceStream == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Resource not found: " + target);
                return;
            }

            // Determine content type based on file extension
            String extension = getExtension(target);
            String contentType = getContentType(extension);
            if (contentType != null) {
                resp.setContentType(contentType);
            } else {
                resp.setContentType("application/octet-stream");
            }
            //resp.setHeader("Cache-Control", "max-age=1800"); //加入客户端缓存，30分钟过期

            if( ".js".equals(extension) || ".html".equals(extension) || ".css".equals(extension)) {

                //System.out.println("Performing character replacement for resource: " + target);
                // Perform character replacement for JavaScript and HTML files
                String content = IOUtils.toString(resourceStream, "UTF-8");
                content = content.replace(PATH_TOBE_REPLACE, req.getContextPath());
                resp.getWriter().write(content);
            }
            else {
                // Write the resource content to the response
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = resourceStream.read(buffer)) != -1) {
                    resp.getOutputStream().write(buffer, 0, bytesRead);
                }
            }
        }

    }


    private String getExtension(String target) {
        int lastDot = target.lastIndexOf('.');
        if (lastDot != -1 && lastDot < target.length() - 1) {
            return target.substring(lastDot).toLowerCase();
        }
        return null;
    }

    private String getContentType(String extension) {
        return (StringUtils.isNotEmpty(extension)) ? CONTENT_TYPES.get(extension.toLowerCase()) : null;
    }

}
