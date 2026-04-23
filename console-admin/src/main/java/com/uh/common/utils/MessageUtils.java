package com.uh.common.utils;

import com.uh.common.config.ConsoleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * 获取i18n资源文件
 *
 * @author XiaoZhangTongZhi
 */
public class MessageUtils
{
    private static final Logger logger = LoggerFactory.getLogger(MessageUtils.class);

    private static final String MESSAGES_PATH = "i18n/messages";

    private static Map<Locale, Properties> properties = new HashMap<>(2);

    private static Locale configLocale = null;


    public static String message(String msgCode, Object... args) {
        return message(msgCode, getDefaultLocale(), args);
    }

    /**
     * 根据消息键和参数 获取消息 委托给spring messageSource
     *
     * @param msgCode 消息键
     * @param args 参数
     * @return 获取国际化翻译值
     */
    public static String message(String msgCode, Locale locale, Object... args) {
        String msg = getProperties(locale).getProperty(msgCode);
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                msg = msg.replace("{" + i + "}", args[i].toString());
            }
        }
        return msg;
    }


    private static Locale getDefaultLocale() {

        if (configLocale != null) {
            return configLocale;
        }
        else {
            if(StringUtils.isNotEmpty(ConsoleConfig.readLocale())) {
                // 使用配置文件中指定的语言标签
                String formattedTag = ConsoleConfig.readLocale().replace('_', '-'); // 替换下划线为连字符，符合Locale的标准格式
                configLocale = Locale.forLanguageTag(formattedTag);
                logger.info("Using configured locale: {}", configLocale.toLanguageTag());
            }

            if(configLocale != null) {
                return configLocale;
            }
            else {
                return  Locale.SIMPLIFIED_CHINESE; // 默认使用简体中文
            }
        }
    }


    private static Properties getProperties(Locale locale) {
        Properties prop = properties.get(locale);
        if (prop == null) {
            prop = new Properties();

            String languageTag = locale.toLanguageTag(); // 替换下划线为连字符，符合Locale的标准格式

            //判断是否存在对应的国际化文件
            URL url = MessageUtils.class.getClassLoader().getResource(MESSAGES_PATH + "_" + languageTag + ".properties");
            if (url == null) {
                url = MessageUtils.class.getClassLoader().getResource(MESSAGES_PATH + ".properties");
            }
            try (InputStreamReader reader = new InputStreamReader(url.openStream(), "UTF-8")) {
                prop.load(reader);
                properties.put(locale, prop);
            } catch (Exception e) {
                throw new RuntimeException("Load i18n messages failed!", e);
            }
        }
        return prop;
    }



}
