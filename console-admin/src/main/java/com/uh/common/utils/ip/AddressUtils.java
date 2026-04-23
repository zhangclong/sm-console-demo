package com.uh.common.utils.ip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.uh.common.config.ConsoleConfig;
import com.uh.common.constant.Constants;
import com.uh.common.utils.StringUtils;
import com.uh.common.utils.http.HttpUtils;

/**
 * 获取地址类
 *
 * @author XiaoZhangTongZhi
 */
public class AddressUtils
{
    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    // IP地址查询
    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    public static String getRealAddressByIP(String ip, boolean resolveLocation)
    {
        // 内网不查询
        if (IpUtils.internalIp(ip))
        {
            return "Intranet IP";
        }
        if (resolveLocation)
        {
            try
            {
                String rspStr = HttpUtils.sendGet(IP_URL, "ip=" + ip + "&json=true", Constants.GBK);
                if (StringUtils.isEmpty(rspStr))
                {
                    log.error("Exception occurred while obtaining geographic location {}", ip);
                    return UNKNOWN;
                }
                JSONObject obj = JSON.parseObject(rspStr);
                String region = obj.getString("pro");
                String city = obj.getString("city");
                return String.format("%s %s", region, city);
            }
            catch (Exception e)
            {
                log.error("Exception occurred while obtaining geographic location {}", ip);
            }
        }
        return UNKNOWN;
    }
}
