package com.uh.common.config;

/**
 * 跨源资源共享(CORS)的配置
 */
public class ServerCorsConfig {

    // 是否启用跨域设置，默认值false
    private boolean enabled = false;

    // 允许的请求源, 默认值"*", 建议生产改为具体的地址，如：http://192.168.0.90:8083
    private String allowedOrigin = "*";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAllowedOrigin() {
        return allowedOrigin;
    }

    public void setAllowedOrigin(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }
}