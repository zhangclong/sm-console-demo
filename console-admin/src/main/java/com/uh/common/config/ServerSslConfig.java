package com.uh.common.config;

/**
 * 服务器SSL配置
 */
public class ServerSslConfig {

    // 使用SSL配置为true
    private boolean enabled = false;

    // 国密证书pfx文件路径
    private String keyStore;

    // 证书别名，一般不用修改。若证书别名非默认请修改。可选值：SIG,ENC
    private String keyAlias = "SIG,ENC";

    // 证书类型，可选值：PKCS12,JKS
    private String keyStoreType = "PKCS12";

    // 证书密码必须和证书pfx文件密码一致
    private String keyStorePassword;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

}