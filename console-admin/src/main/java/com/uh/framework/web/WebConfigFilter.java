package com.uh.framework.web;

import com.uh.common.core.domain.LoginUser;
import com.uh.common.core.tenant.TenantContext;
import com.uh.common.utils.AntPathUtils;
import com.uh.common.utils.ServletUtils;
import com.uh.common.utils.StringUtils;
import com.uh.system.service.SysTenantService;
import com.uh.system.service.TokenService;
import org.springframework.context.ApplicationContext;

import static com.uh.common.constant.FrameworkConstants.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.uh.common.constant.Constants.MINUTE_MILLIS;

public class WebConfigFilter implements Filter{

    static final String APPLICATION_JSON_VALUE = "application/json";

    static final String PATH_HOME_INDEX = "/index";

    private boolean corsEnabled;

    private String corsAllowedOrigin;

    private TokenService tokenService = null;

    private SysTenantService tenantService = null;

    private ApplicationContext applicationContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    public void setCorsEnabled(boolean corsEnabled) {
        this.corsEnabled = corsEnabled;
    }

    public void setCorsAllowedOrigin(String corsAllowedOrigin) {
        this.corsAllowedOrigin = corsAllowedOrigin;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {


        if(response instanceof HttpServletResponse && request instanceof HttpServletRequest) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpServletRequest httpRequest = (HttpServletRequest)request;

            // 设置统一的安全header
            appendHeaders(httpResponse);

            // 跨域设置
            cors(httpResponse);

            // 对 application/json 的request进行包装
            if (StringUtils.startsWithIgnoreCase(request.getContentType(), APPLICATION_JSON_VALUE)) {
                httpRequest = new RepeatedlyRequestWrapper(httpRequest, response);
            }

            if(tokenService == null && applicationContext != null) {
                tokenService = applicationContext.getBean(TokenService.class);
            }
            if(tenantService == null && applicationContext != null) {
                tenantService = applicationContext.getBean(SysTenantService.class);
            }
            LoginUser loginUser = tokenService.getLoginUser(httpRequest);
            String path = httpRequest.getRequestURI();
            String contextPath = httpRequest.getContextPath();

            //去除其中的contextPath部分
            if( StringUtils.isNotEmpty(contextPath) && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }

            if(loginUser == null) {
                if(AntPathUtils.matches(path, PERMIT_LOGINS)
                    && !AntPathUtils.matches(path, PERMIT_LOGINS_EXCLUDES)) {
                    // 401 Unauthorized Request access
                    ServletUtils.renderString(httpResponse, "{\"msg\":\"Request access: " + path + " authentication failed, unable to access!\",\"code\":401}");
                    return; //break, do not continue the filter chain
                }
            }
            else {
                if(AntPathUtils.matches(path, PERMIT_NOT_LOGINS)) {
                    httpResponse.sendRedirect(httpRequest.getContextPath() + PATH_HOME_INDEX); // 登录状态下访问登录页面，自动跳转到首页
                    return; //break, do not continue the filter chain
                }

                if(System.currentTimeMillis() > loginUser.getLoginTime() + MINUTE_MILLIS) {
                    if(!AntPathUtils.matches(path, TOKEN_REFRESH_SKIPS)) {
                        // 如果登录时间超过一分钟, 路径不是需要跳过的定时刷新路径，则刷新token
                        //System.out.println("Refreshing token for user: " + loginUser.getUsername() + ", path: " + path);
                        tokenService.refreshLoginUser(loginUser); // 刷新token
                    }
                }

                // 自动选择默认租户（currentTenantId 为空时从可访问租户中选第一个）
                if (loginUser.getCurrentTenantId() == null && tenantService != null)
                {
                    java.util.List<Long> tenantIds = tenantService.selectTenantIdsByUserId(loginUser.getUserId());
                    if (!tenantIds.isEmpty())
                    {
                        loginUser.setCurrentTenantId(tenantIds.get(0));
                        loginUser.setTenantIds(tenantIds);
                        tokenService.refreshLoginUser(loginUser);
                    }
                }
            }

            // 已登录时将 currentTenantId 注入 TenantContext，请求结束后在 finally 中清除
            if (loginUser != null && loginUser.getCurrentTenantId() != null)
            {
                TenantContext.setTenantId(loginUser.getCurrentTenantId());
            }
            try
            {
                filterChain.doFilter(httpRequest, httpResponse);
            }
            finally
            {
                TenantContext.clear();
            }
        }
        else {
            filterChain.doFilter(request, response);
        }


    }

    private void appendHeaders(HttpServletResponse httpResponse) {
        httpResponse.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains;"); // 设置统一的header
        httpResponse.addHeader("X-Frame-Options", "ALLOWALL");
        httpResponse.addHeader("Referrer-Policy", "no-referrer");
        httpResponse.addHeader("X-Download-Options", "noopen");
        httpResponse.addHeader("X-Permitted-Cross-Domain-Policies", "none");
        httpResponse.addHeader("Content-Security-Policy",
                "default-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data:;" +
                        "font-src 'self' 'unsafe-inline' http://at.alicdn.com;" +
                        "frame-ancestors 'self'");
    }


    private void cors(HttpServletResponse response) {
        if (corsEnabled) {
            response.setHeader("Access-Control-Allow-Origin", corsAllowedOrigin);
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "1800");
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
