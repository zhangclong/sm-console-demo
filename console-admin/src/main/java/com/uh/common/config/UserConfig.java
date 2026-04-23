package com.uh.common.config;


public class UserConfig {
    //登录最多重试次数
    private int loginMaxRetries;

    //密码过期时长(天)
    private int passwordExpireDays;

    //用户管理-账号初始密码
    private String initPassword;

    //admin的初始化密码
    private String initAdminPassword;

    private TokenConfig token;

    public int getLoginMaxRetries() {
        return loginMaxRetries;
    }

    public void setLoginMaxRetries(int loginMaxRetries) {
        this.loginMaxRetries = loginMaxRetries;
    }

    public int getPasswordExpireDays() {
        return passwordExpireDays;
    }

    public  void setPasswordExpireDays(int passwordExpireDays) {
        this.passwordExpireDays = passwordExpireDays;
    }

    public String getInitPassword() {
        return initPassword;
    }

    public void setInitPassword(String initPassword) {
        this.initPassword = initPassword;
    }

    public String getInitAdminPassword() {
        return initAdminPassword;
    }

    public void setInitAdminPassword(String initAdminPassword) {
        this.initAdminPassword = initAdminPassword;
    }

    public TokenConfig getToken() {
        return token;
    }

    public void setToken(TokenConfig token) {
        this.token = token;
    }

    public static class TokenConfig  {
        //令牌自定义标识
        private String header;
        //令牌密钥
        private String secret;
        //令牌有效期（默认30分钟）
        private int expireTime;
        //是否允许账户多终端同时登录（true允许 false不允许）
        private boolean multiLogin;

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public int getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(int expireTime) {
            this.expireTime = expireTime;
        }

        public boolean isMultiLogin() {
            return multiLogin;
        }

        public void setMultiLogin(boolean multiLogin) {
            this.multiLogin = multiLogin;
        }
    }
}
