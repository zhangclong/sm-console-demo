package com.uh.common.config;


import java.util.ArrayList;
import java.util.List;


public class InterfaceConfig {

    boolean enable;

    List<String>  usernames;

    List<String>  allowedIps;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }

    public List<String> getAllowedIps() {
        return allowedIps;
    }

    public void setAllowedIps(List<String> allowedIps) {
        this.allowedIps = allowedIps;
    }

    public boolean isAllowedUsername(String username) {
        if (usernames == null || usernames.isEmpty()) {
            return false; // 如果没有配置用户名，则不允许所有用户
        }
        return usernames.contains(username);
    }

    public boolean isAllowedIp(String ip) {
        if (allowedIps == null || allowedIps.isEmpty()) {
            return false; // 如果没有配置IP，则不允许所有IP
        }

        //return allowedIps.contains(ip);
        //allowedIps格式中，可能包含*的通配符，需要逐一和ip进行匹配
        for (String allowedIp : allowedIps) {
            if (allowedIp.equals(ip)) {
                return true; // 直接匹配或通配符匹配
            }
            if (allowedIp.contains("*")) {
                //将allowedIp中的’.‘替换为正则表达式中的转义符，再将*替换为正则表达式中的.*
                String regex = allowedIp.replace(".", "\\.").replace("*", ".*");
                if (ip.matches(regex)) {
                    return true; // 正则匹配
                }
            }
        }
        return false;
    }


    public static void main(String args[]) {
        InterfaceConfig config = new InterfaceConfig();
        List<String> allowedIps = new ArrayList<>();
        allowedIps.add("192.168.1.*");
        allowedIps.add("127.0.0.*");
        allowedIps.add("192.168.2.5");
        config.setAllowedIps(allowedIps);

        System.out.println("192.168.1.8 is allowed: " + config.isAllowedIp("192.168.1.8"));
        System.out.println("192.168.2.5 is allowed: " + config.isAllowedIp("192.168.2.5"));
        System.out.println("192.168.2.6 is allowed: " + config.isAllowedIp("192.168.2.6"));
        System.out.println("192.10.2.6 is allowed: " + config.isAllowedIp("192.10.2.6"));
    }
}
