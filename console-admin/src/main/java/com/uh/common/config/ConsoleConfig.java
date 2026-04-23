package com.uh.common.config;


/**
 * 读取项目相关配置
 *
 * @author XiaoZhangTongZhi
 */
public class ConsoleConfig
{
    /** 项目名称 */
    private String name;

    /** 版本 */
    private String version;

    /** 构建时间 */
    private String buildTime;

    /** 版权年份 */
    private String copyrightYear;

    /** 登录日志中，获取访问方地址开关 */
    private boolean addressEnabled;

    /** 验证码类型 */
    private String captchaType;

    /** 管理员密码过期天数，0 表示不过期 */
    private int adminPasswordExpireDays;

//    /** SSH 连接时的超时时间, 单位毫秒 */
//    private static int sshConnectTimeout = 10000;
//
//    /** SSH 命令执行通道超时时间, 单位毫秒 */
//    private static int sshChannelTimeout = 20000;

//    /** 和中心节点管理端口进行通信时的授权码 */
//    private String centerAuthKey;
//
//    /** 和节点管理器进行通信时的授权码 */
//    private String probeAuthKey;

//    /**
//     * 在数据维护中，当数据超过多大时不向前端传递原始value 例如：String类型 value 超过多少字节后前端不显示原始信息 单位 字节
//     */
//    private long maxDataSize;
//
//    /**
//     * License Server 对应的企业版产品秘钥
//     */
//    private String hostProductKey;
//
//    /**
//     * License Server 对应的云原生版产品秘钥
//     */
//    private String k8sProductKey;

    /**
     * 国际化语言配置
     */
    private static String locale;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    public String getCopyrightYear() {
        return copyrightYear;
    }

    public void setCopyrightYear(String copyrightYear) {
        this.copyrightYear = copyrightYear;
    }

    public boolean isAddressEnabled() {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled) {
        this.addressEnabled = addressEnabled;
    }

    public String getCaptchaType() {
        return captchaType;
    }

    public void setCaptchaType(String captchaType) {
        this.captchaType = captchaType;
    }

    public int getAdminPasswordExpireDays() {
        return adminPasswordExpireDays;
    }

    public void setAdminPasswordExpireDays(int adminPasswordExpireDays) {
        this.adminPasswordExpireDays = adminPasswordExpireDays;
    }

//    public int getSshConnectTimeout() {
//        return sshConnectTimeout;
//    }

//    /**
//     * 读取 SSH 连接时的超时时间, 提供静态化访问
//     * @return
//     */
//    public static int readSshConnectTimeout() {
//        return sshConnectTimeout;
//    }
//
//    public void setSshConnectTimeout(int sshConnectTimeout) {
//        this.sshConnectTimeout = sshConnectTimeout;
//    }
//
//    public int getSshChannelTimeout() {
//        return sshChannelTimeout;
//    }
//
//    /**
//     * 读取 SSH 命令执行通道超时时间, 提供静态化访问
//     * @return
//     */
//    public static int readSshChannelTimeout() {
//        return sshChannelTimeout;
//    }
//
//    public void setSshChannelTimeout(int sshChannelTimeout) {
//        this.sshChannelTimeout = sshChannelTimeout;
//    }

//    public String getCenterAuthKey() {
//        return centerAuthKey;
//    }
//
//    public void setCenterAuthKey(String centerAuthKey) {
//        this.centerAuthKey = centerAuthKey;
//    }
//
//    public String getProbeAuthKey() {
//        return probeAuthKey;
//    }
//
//    public void setProbeAuthKey(String probeAuthKey) {
//        this.probeAuthKey = probeAuthKey;
//    }
//
//    public long getMaxDataSize() {
//        return maxDataSize;
//    }
//
//    public void setMaxDataSize(long maxDataSize) {
//        this.maxDataSize = maxDataSize;
//    }
//
//    public String getHostProductKey() {
//        return hostProductKey;
//    }
//
//    public void setHostProductKey(String hostProductKey) {
//        this.hostProductKey = hostProductKey;
//    }
//
//    public String getK8sProductKey() {
//        return k8sProductKey;
//    }
//
//    public void setK8sProductKey(String k8sProductKey) {
//        this.k8sProductKey = k8sProductKey;
//    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * 读取国际化语言配置, 提供静态化访问
     * @return locale 国际化语言配置
     */
    public static String readLocale() {
        return locale;
    }



}
