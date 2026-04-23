package com.uh.console.utils;

import com.uh.common.core.domain.LoginUser;
import com.uh.common.core.domain.PasswordProvider;
import com.uh.common.utils.AESUtils;
import com.uh.common.utils.SecurityUtils;

import static com.uh.common.utils.StringUtils.isNotEmpty;

public class RdsServiceUtils {

    /**
     * PasswordProvider中的密码加密处理。
     * 只有是非接口登录时才进行加密处理。
     * @param provider
     */
    public static void encryptPasswordProvider(PasswordProvider provider) {
        if(provider != null) {
            if(isInterfaceLogin() == false && isNotEmpty(provider.getPassword())) {
                String password = AESUtils.encryptAES(provider.getPassword());
                provider.setPassword(password);
            }
        }
    }

    /**
     * PasswordProvider中的密码解密处理。
     * 只有是非接口登录时才进行解密处理。
     * @param provider
     */
    public static void decryptPasswordProvider(PasswordProvider provider) {

        if(provider != null) {
            if (isInterfaceLogin() == false && isNotEmpty(provider.getPassword())) {
                String password = AESUtils.decryptAES(provider.getPassword());
                provider.setPassword(password);
            }
        }
    }

    public static boolean isInterfaceLogin() {
        LoginUser user = SecurityUtils.getLoginUser();
        if(user != null) {
            return user.isInterfaceLogin();
        }

        return false;
    }


}
