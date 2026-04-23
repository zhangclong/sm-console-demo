package com.uh.common.core.domain;

import java.util.Collection;
import java.util.Set;

import com.uh.system.domain.SysUser;

/**
 * 登录用户身份权限
 *
 * @author XiaoZhangTongZhi
 */
public class LoginUser
{
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否为接口客户端登录
     */
    private boolean interfaceLogin = false;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 登录时间（更新时间）
     */
    private long loginTime;

    /**
     * 登录过期时间
     */
    private long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 用户信息
     */
    private SysUser user;

    /**
     * 当前会话选中的租户ID（服务端存储，不放入JWT）
     */
    private Long currentTenantId;

    /**
     * 用户可访问的租户ID列表（用于默认自动选择）
     */
    private java.util.List<Long> tenantIds;

    public Long getUserId()
    {
        return userId;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public LoginUser()
    {
    }

    public LoginUser(SysUser user, Set<String> permissions)
    {
        this.user = user;
        this.userId = user.getUserId();
        this.permissions = permissions;
    }

    public String getPassword()
    {
        return user.getPassword();
    }

    public String getUsername()
    {
        return user.getUserName();
    }

    public long getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime(long loginTime)
    {
        this.loginTime = loginTime;
    }

    public String getIpaddr()
    {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }

    public boolean isInterfaceLogin() {
        return interfaceLogin;
    }

    public void setInterfaceLogin(boolean interfaceLogin) {
        this.interfaceLogin = interfaceLogin;
    }

    public String getLoginLocation()
    {
        return loginLocation;
    }

    public void setLoginLocation(String loginLocation)
    {
        this.loginLocation = loginLocation;
    }

    public String getBrowser()
    {
        return browser;
    }

    public void setBrowser(String browser)
    {
        this.browser = browser;
    }

    public String getOs()
    {
        return os;
    }

    public void setOs(String os)
    {
        this.os = os;
    }

    public long getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(long expireTime)
    {
        this.expireTime = expireTime;
    }

    public Set<String> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Set<String> permissions)
    {
        this.permissions = permissions;
    }

    public SysUser getUser()
    {
        return user;
    }

    public void setUser(SysUser user)
    {
        this.user = user;
    }

    public Long getCurrentTenantId()
    {
        return currentTenantId;
    }

    public void setCurrentTenantId(Long currentTenantId)
    {
        this.currentTenantId = currentTenantId;
    }

    public java.util.List<Long> getTenantIds()
    {
        return tenantIds;
    }

    public void setTenantIds(java.util.List<Long> tenantIds)
    {
        this.tenantIds = tenantIds;
    }

}
