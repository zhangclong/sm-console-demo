package com.uh.system.service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.uh.common.config.ConsoleConfig;
import com.uh.common.config.UserConfig;
import com.uh.common.utils.spring.SpringUtils;
import com.uh.framework.cache.ObjectCache;
import org.springframework.stereotype.Component;
import com.uh.common.constant.CacheConstants;
import com.uh.common.constant.Constants;
import com.uh.common.core.domain.LoginUser;
import com.uh.common.utils.ServletUtils;
import com.uh.common.utils.ip.AddressUtils;
import com.uh.common.utils.ip.IpUtils;
import com.uh.common.utils.uuid.IdUtils;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import static com.uh.common.constant.Constants.MINUTE_MILLIS;
import static com.uh.common.utils.StringUtils.isNotEmpty;


/**
 * token验证处理
 *
 *
 * @author XiaoZhangTongZhi
 */
@Component
public class TokenService
{
    /**
     * 令牌前缀
     */
    public static final String CLAIM_KEY = "login_user_key";

    // 令牌秘钥
    private String secret;

    // 令牌有效期（默认30分钟）
    private int expireTime;

    // 是否允许账户多终端同时登录（true允许 false不允许）
    private boolean multiLogin;

    @Resource
    private ObjectCache cacheService;

    @Resource
    private ConsoleConfig consoleConf;

    @PostConstruct
    public void init() {
        UserConfig userConfig = SpringUtils.getBean(UserConfig.class);
        UserConfig.TokenConfig token = userConfig.getToken();

        this.secret = token.getSecret();
        this.expireTime = token.getExpireTime();
        this.multiLogin = token.isMultiLogin();
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser(HttpServletRequest request)
    {
        if(request != null) {
            // 获取请求携带的令牌
            String encodedToken = getEncodedToken(request);
            if (isNotEmpty(encodedToken)) {
                // 解析对应的权限以及用户信息
                String token = decodeToken(encodedToken);
                return cacheService.getCacheObject(getTokenCacheKey(token));
            }
        }
        return null;
    }

    /**
     * 获得所有在线用户的列表
     * @return 返回所有在线用户的列表
     */
    public List<LoginUser> getLoginUsers() {
        Collection<String> keys = cacheService.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        List<LoginUser> userOnlineList = new ArrayList<>();
        for (String key : keys) {
            LoginUser user = cacheService.getCacheObject(key);
            if(user != null) {
                userOnlineList.add(user);
            }
        }

        return userOnlineList;
    }

    /**
     * 删除用户身份信息
     */
    public boolean delLoginUser(String token)
    {
        if (isNotEmpty(token))
        {
            String userKey = getTokenCacheKey(token);

            LoginUser user = cacheService.getCacheObject(userKey);
            if(user != null) {
                cacheService.deleteObject(userKey);

                if (!multiLogin)
                {
                    String userIdKey = getUserIdCacheKey(user.getUserId());
                    cacheService.deleteObject(userIdKey);
                }
                return true;
            }
        }

        return false;
    }

    /**
     * 如果不允许多终端登录，则删除之前的登录用户信息
     *
     * @param userId 用户 ID
     * @return 是否删除成功
     */
    public boolean delLoginUserByUserId(Long userId)
    {
        if (!multiLogin && userId != null) {
            String userIdKey = getUserIdCacheKey(userId);
            String userKey = cacheService.getCacheObject(userIdKey);
            if (isNotEmpty(userKey)) {
                cacheService.deleteObject(userKey);
                cacheService.deleteObject(userIdKey);
                return true;
            }
        }
        return false;
    }





    /**
     * 加入用户登录信息，创建并返回加密后的令牌
     *
     * @param loginUser 用户信息
     * @return 加密后的令牌
     */
    public String addLoginUser(LoginUser loginUser)
    {
        String token = IdUtils.fastUUID();
        loginUser.setToken(token);
        setRequestInfo(loginUser);
        refreshLoginUser(loginUser);
        return encodeToken(token);
    }

    /**
     * 刷新缓存中令牌有效期
     *
     * @param loginUser 登录信息
     */
    public synchronized void refreshLoginUser(LoginUser loginUser)
    {
        if (loginUser != null && isNotEmpty(loginUser.getToken())) {
            loginUser.setLoginTime(System.currentTimeMillis());
            loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MINUTE_MILLIS);
            // 根据uuid将loginUser缓存
            String userKey = getTokenCacheKey(loginUser.getToken());
            cacheService.setCacheObject(userKey, loginUser, expireTime, TimeUnit.MINUTES);

            if (!multiLogin) {
                // 缓存用户唯一标识，防止同一帐号，同时登录
                String userIdKey = getUserIdCacheKey(loginUser.getUser().getUserId());
                cacheService.setCacheObject(userIdKey, userKey, expireTime, TimeUnit.MINUTES);
            }
        }
    }

    /**
     * 通过HTTP Request信息，补充登录信息。包括：User-Agent, IP地址, 登录地理位置，操作系统，浏览器类型
     *
     * @param loginUser 登录信息
     */
    private void setRequestInfo(LoginUser loginUser)
    {
        UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
        loginUser.setIpaddr(ip);
        loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip, consoleConf.isAddressEnabled()));
        loginUser.setBrowser(userAgent.getBrowser().getName());
        loginUser.setOs(userAgent.getOperatingSystem().getName());
    }

    /**
     * 把UUID格式的令牌进行加密
     *
     * @param token 令牌（UUID格式）
     * @return 加密令牌
     */
    private String encodeToken(String token)
    {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY, token);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    /**
     * 把加密令牌进行解密，获取UUID格式的令牌
     *
     * @param encodedToken 加密令牌
     * @return 令牌（UUID格式）（UUID格式）
     */
    private String decodeToken(String encodedToken)
    {
        Claims claims =  Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(encodedToken)
                .getBody();

        // 解析对应的权限以及用户信息
        return  (String) claims.get(CLAIM_KEY);
    }


    /**
     * 获取请求token
     *
     * @param request HTTP 请求
     * @return token
     */
    private String getEncodedToken(HttpServletRequest request)
    {
        String token = request.getHeader(Constants.TOKEN_HEADER);
        if (isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    private String getTokenCacheKey(String uuid)
    {
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }

    private String getUserIdCacheKey(Long userId)
    {
        return CacheConstants.LOGIN_USERID_KEY + userId;
    }



}
