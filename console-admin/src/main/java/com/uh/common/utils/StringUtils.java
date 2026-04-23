package com.uh.common.utils;

import com.uh.common.constant.Constants;
import com.uh.common.core.text.StrFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 字符串工具类
 *
 * @author XiaoZhangTongZhi
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils
{

    public static long SIZE_KB = 1024L;
    public static long SIZE_MB = 1048576L;
    public static long SIZE_GB = 1073741824L;
    public static long SIZE_TB = 1099511627776L;
    public static long SIZE_PB = 1125899906842624L;

    /** 空字符串 */
    private static final String NULLSTR = "";

    /** 下划线 */
    private static final char SEPARATOR = '_';

    /**
     * The Unix line separator string.
     */
    public static final String LINE_SEPARATOR_UNIX = "\n";

    /**
     * The Windows line separator string.
     */
    public static final String LINE_SEPARATOR_WINDOWS = "\r\n";

    /**
     * 获取参数不为空值
     *
     * @param value defaultValue 要判断的value
     * @return value 返回值
     */
    public static <T> T nvl(T value, T defaultValue)
    {
        return value != null ? value : defaultValue;
    }

    /**
     * * 判断一个Collection是否为空， 包含List，Set，Queue
     *
     * @param coll 要判断的Collection
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Collection<?> coll)
    {
        return isNull(coll) || coll.isEmpty();
    }

    /**
     * * 判断一个Collection是否非空，包含List，Set，Queue
     *
     * @param coll 要判断的Collection
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Collection<?> coll)
    {
        return !isEmpty(coll);
    }

    /**
     * * 判断一个对象数组是否为空
     *
     * @param objects 要判断的对象数组
     ** @return true：为空 false：非空
     */
    public static boolean isEmpty(Object[] objects)
    {
        return isNull(objects) || (objects.length == 0);
    }

    /**
     * * 判断一个对象数组是否非空
     *
     * @param objects 要判断的对象数组
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Object[] objects)
    {
        return !isEmpty(objects);
    }

    /**
     * * 判断一个Map是否为空
     *
     * @param map 要判断的Map
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Map<?, ?> map)
    {
        return isNull(map) || map.isEmpty();
    }

    /**
     * * 判断一个Map是否为空
     *
     * @param map 要判断的Map
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Map<?, ?> map)
    {
        return !isEmpty(map);
    }

    /**
     * * 判断一个字符串是否为空串
     *
     * @param str String
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(String str)
    {
        return isNull(str) || NULLSTR.equals(str.trim());
    }

    /**
     * * 判断一个字符串是否为非空串
     *
     * @param str String
     * @return true：非空串 false：空串
     */
    public static boolean isNotEmpty(String str)
    {
        return !isEmpty(str);
    }

    /**
     * * 判断一个对象是否为空
     *
     * @param object Object
     * @return true：为空 false：非空
     */
    public static boolean isNull(Object object)
    {
        return object == null;
    }

    /**
     * * 判断一个对象是否非空
     *
     * @param object Object
     * @return true：非空 false：空
     */
    public static boolean isNotNull(Object object)
    {
        return !isNull(object);
    }


    /**
     * 去空格
     */
    public static String trim(String str)
    {
        return (str == null ? "" : str.trim());
    }

    public static String trim(String str, int maxLine, int maxLength) {
        if(str == null) return str;

        //消减为指定长度限制以下
        int len = str.length();
        if(len > maxLength) {
            str = str.substring(0, maxLength);
        }

        //消减为指定行数以下
        int lineNumber = 0;
        StringBuilder resultBuilder = new StringBuilder();
        for (int i=0; i<str.length(); i++) {
            if (i == str.indexOf("\n", i)) {
                lineNumber ++;
                if (lineNumber >= maxLine) {
                    break;
                }
            }

            char c = str.charAt(i);
            resultBuilder.append(c);
        }

        return resultBuilder.toString();
    }

    /**
     * 截取字符串
     *
     * @param str 字符串
     * @param start 开始
     * @return 结果
     */
    public static String substring(final String str, int start)
    {
        if (str == null)
        {
            return NULLSTR;
        }

        if (start < 0)
        {
            start = str.length() + start;
        }

        if (start < 0)
        {
            start = 0;
        }
        if (start > str.length())
        {
            return NULLSTR;
        }

        return str.substring(start);
    }

    /**
     * 截取字符串
     *
     * @param str 字符串
     * @param start 开始
     * @param end 结束
     * @return 结果
     */
    public static String substring(final String str, int start, int end)
    {
        if (str == null)
        {
            return NULLSTR;
        }

        if (end < 0)
        {
            end = str.length() + end;
        }
        if (start < 0)
        {
            start = str.length() + start;
        }

        if (end > str.length())
        {
            end = str.length();
        }

        if (start > end)
        {
            return NULLSTR;
        }

        if (start < 0)
        {
            start = 0;
        }
        if (end < 0)
        {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * 格式化文本, {} 表示占位符<br>
     * 此方法只是简单将占位符 {} 按照顺序替换为参数<br>
     * 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可<br>
     * 例：<br>
     * 通常使用：format("this is {} for {}", "a", "b") -> this is a for b<br>
     * 转义{}： format("this is \\{} for {}", "a", "b") -> this is \{} for a<br>
     * 转义\： format("this is \\\\{} for {}", "a", "b") -> this is \a for b<br>
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param params 参数值
     * @return 格式化后的文本
     */
    public static String format(String template, Object... params)
    {
        if (isEmpty(params) || isEmpty(template))
        {
            return template;
        }
        return StrFormatter.format(template, params);
    }

    /**
     * 是否为http(s)://开头
     *
     * @param link 链接
     * @return 结果
     */
    public static boolean ishttp(String link)
    {
        return StringUtils.startsWithAny(link, Constants.HTTP, Constants.HTTPS);
    }

    /**
     * 字符串转list
     *
     * @param str 字符串
     * @param sep 分隔符
     * @param filterBlank 过滤纯空白
     * @param trim 去掉首尾空白
     * @return list集合
     */
    public static final List<String> str2List(String str, String sep, boolean filterBlank, boolean trim)
    {
        List<String> list = new ArrayList<String>();
        if (StringUtils.isEmpty(str))
        {
            return list;
        }

        // 过滤空白字符串
        if (filterBlank && StringUtils.isBlank(str))
        {
            return list;
        }
        String[] split = str.split(sep);
        for (String string : split)
        {
            if (filterBlank && StringUtils.isBlank(string))
            {
                continue;
            }
            if (trim)
            {
                string = string.trim();
            }
            list.add(string);
        }

        return list;
    }


    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串同时串忽略大小写
     *
     * @param cs 指定字符串
     * @param searchCharSequences 需要检查的字符串数组
     * @return 是否包含任意一个字符串
     */
    public static boolean containsAnyIgnoreCase(CharSequence cs, CharSequence... searchCharSequences)
    {
        if (isEmpty(cs) || isEmpty(searchCharSequences))
        {
            return false;
        }
        for (CharSequence testStr : searchCharSequences)
        {
            if (containsIgnoreCase(cs, testStr))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 驼峰转下划线命名
     */
    public static String toUnderScoreCase(String str)
    {
        if (str == null)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // 前置字符是否大写
        boolean preCharIsUpperCase = true;
        // 当前字符是否大写
        boolean curreCharIsUpperCase = true;
        // 下一字符是否大写
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (i > 0)
            {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            }
            else
            {
                preCharIsUpperCase = false;
            }

            curreCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1))
            {
                nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase)
            {
                sb.append(SEPARATOR);
            }
            else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase)
            {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    /**
     * 是否包含字符串
     *
     * @param str 验证字符串
     * @param strs 字符串组
     * @return 包含返回true
     */
    public static boolean inStringIgnoreCase(String str, String... strs)
    {
        if (str != null && strs != null)
        {
            for (String s : strs)
            {
                if (str.equalsIgnoreCase(trim(s)))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。 例如：HELLO_WORLD->HelloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String convertToCamelCase(String name, String separator, boolean upperCaseFirst)
    {
        StringBuilder result = new StringBuilder();
        // 快速检查
        if (name == null || name.isEmpty()) { // 没必要转换
            return name;
        }
        else if (!name.contains(separator))  {// 不含下划线
            if(upperCaseFirst)
                return name.substring(0, 1).toUpperCase() + name.substring(1); // 不含下划线，仅将首字母大写
            else
                return name;
        }
        // 用下划线将原始字符串分割
        String[] camels = name.split(separator);
        boolean first = true;
        for (String camel : camels)
        {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty())
            {
                continue;
            }

            if (first && !upperCaseFirst) {
                // 首字母不大写
                result.append(camel.toLowerCase());
            } else {
                // 首字母大写
                result.append(camel.substring(0, 1).toUpperCase());
                result.append(camel.substring(1).toLowerCase());
            }

            first = false;
        }
        return result.toString();
    }



    /**
     * 驼峰式命名法 例如：user_name->userName
     */
    public static String toCamelCase(String s)
    {
        if (s == null)
        {
            return null;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);

            if (c == SEPARATOR)
            {
                upperCase = true;
            }
            else if (upperCase)
            {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            }
            else
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    /**
     * 数字左边补齐0，使之达到指定长度。注意，如果数字转换为字符串后，长度大于size，则只保留 最后size个字符。
     *
     * @param num 数字对象
     * @param size 字符串指定长度
     * @return 返回数字的字符串格式，该字符串为指定长度。
     */
    public static final String padl(final Number num, final int size)
    {
        return padl(num.toString(), size, '0');
    }

    /**
     * 字符串左补齐。如果原始字符串s长度大于size，则只保留最后size个字符。
     *
     * @param s 原始字符串
     * @param size 字符串指定长度
     * @param c 用于补齐的字符
     * @return 返回指定长度的字符串，由原字符串左补齐或截取得到。
     */
    public static final String padl(final String s, final int size, final char c)
    {
        final StringBuilder sb = new StringBuilder(size);
        if (s != null)
        {
            final int len = s.length();
            if (s.length() <= size)
            {
                for (int i = size - len; i > 0; i--)
                {
                    sb.append(c);
                }
                sb.append(s);
            }
            else
            {
                return s.substring(len - size, len);
            }
        }
        else
        {
            for (int i = size; i > 0; i--)
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 把内存和磁盘大小的整数值（long类型），转换为以 KB、MB、GB、TB、PB为单位的字符串显示，取值精确到小数后1位。
     *
     * @param size 字节大小
     * @return 转换后值
     */
    public static String toReadableSize(long size)
    {
        if(size >= SIZE_PB) {
            double f = (double) size / SIZE_PB;
            return String.format(f > 100 ? "%.0fPB" : "%.1fPB", f);
        }
        else if(size >= SIZE_TB) {
            double f = (double) size / SIZE_TB;
            return String.format(f > 100 ? "%.0fTB" : "%.1fTB", f);
        }
        else if (size >= SIZE_GB)
        {
            double f = (double) size / SIZE_GB;
            return String.format(f > 100 ? "%.0fGB" : "%.1fGB", f);
        }
        else if (size >= SIZE_MB)
        {
            double f = (double) size / SIZE_MB;
            return String.format(f > 100 ? "%.0fMB" : "%.1fMB", f);
        }
        else if (size >= SIZE_KB)
        {
            double f = (double) size / SIZE_KB;
            return String.format(f > 100 ? "%.0fKB" : "%.1fKB", f);
        }
        else
        {
            return String.format("%dB", size);
        }
    }


    /**
     * 把字符串中的Windows换行符（\r\n）转换为Unix换行符（\n）。
     * @param input
     * @return
     */
    public static String convertToUnix(String input) {
        if(input != null && input.contains(LINE_SEPARATOR_WINDOWS)) {
            return input.replace(LINE_SEPARATOR_WINDOWS, LINE_SEPARATOR_UNIX);
        }
        else {
            return input;
        }
    }

    /**
     * Give a static method for this requirement:
     *    Returns true the key matching the glob-style pattern.
     *    Glob style patterns examples:
     *    h?llo will match hello hallo hhllo
     *    h*llo will match hllo heeeello
     *    h[ae] llo will match hello and hallo, but not hillo
     *    Use \ to escape special chars if you want to match them verbatim.
     * @param key
     * @param pattern
     * @return
     */
    public static boolean matchesGlobPattern(String key, String pattern) {
        String regex = patternToRegex(pattern);
        return key.matches(regex);
    }

    private static String patternToRegex(String pattern) {
        StringBuilder regex = new StringBuilder();
        boolean escaping = false;
        int inBrackets = 0;

        for (char currentChar : pattern.toCharArray()) {
            switch (currentChar) {
                case '*':
                    if (escaping) {
                        regex.append("\\*");
                        escaping = false;
                    } else {
                        regex.append(".*");
                    }
                    break;
                case '?':
                    if (escaping) {
                        regex.append("\\?");
                        escaping = false;
                    } else {
                        regex.append('.');
                    }
                    break;
                case '[':
                    if (escaping) {
                        regex.append("\\[");
                        escaping = false;
                    } else {
                        inBrackets++;
                        regex.append('[');
                    }
                    break;
                case ']':
                    if (inBrackets > 0 && !escaping) {
                        inBrackets--;
                        regex.append(']');
                    } else if (escaping) {
                        regex.append("\\]");
                        escaping = false;
                    } else {
                        regex.append("\\]");
                    }
                    break;
                case '\\':
                    if (escaping) {
                        regex.append("\\\\");
                        escaping = false;
                    } else {
                        escaping = true;
                    }
                    break;
                default:
                    if (escaping) {
                        regex.append("\\");
                        escaping = false;
                    }
                    regex.append(currentChar);
            }
        }

        return regex.toString();
    }


    public static void main(String[] args) {
//        System.out.println(toReadableSize(3300));
//        System.out.println(toReadableSize(2048));
//        System.out.println(toReadableSize(1024*1024));
//        System.out.println(toReadableSize(1024L*1024L*1024L*1020L));
//        System.out.println(toReadableSize(1099511627776L));
//        System.out.println(toReadableSize(24L*1024L*1024L*1024L*1024L));
//        System.out.println(toReadableSize(2L*1024L*1024L*1024L*1024L*1024L*1024L));
//
//
//        System.out.println(matchesGlobPattern("hello", "h?llo"));
//        System.out.println(matchesGlobPattern("hallo", "h?llo"));
//        System.out.println(matchesGlobPattern("hhllo", "h?llo"));
//        System.out.println(matchesGlobPattern("hllo", "h*llo"));
//        System.out.println(matchesGlobPattern("heeeello", "h*llo"));
//        System.out.println(matchesGlobPattern("hello", "h[ae]llo"));
//        System.out.println(matchesGlobPattern("hallo", "h[ae]llo"));
//        System.out.println(matchesGlobPattern("hillo", "h[ae]llo"));
//        System.out.println(matchesGlobPattern("hello", "h\\?llo"));
//        System.out.println(matchesGlobPattern("h?llo", "h\\?llo"));
//        System.out.println(matchesGlobPattern("h\\?llo", "h\\?llo"));
//        System.out.println(matchesGlobPattern("h\\?llo", "h?llo"));
//        System.out.println(matchesGlobPattern("h\\?llo", "h\\?llo"));
//        System.out.println(matchesGlobPattern("h\\?llo", "h\\?llo"));

        System.out.println(convertToCamelCase("hello_world_test", "_", true));
        System.out.println(convertToCamelCase("hello_world_test", "_", false));
        System.out.println(convertToCamelCase("hello__world__test", "_", true));
        System.out.println(convertToCamelCase("hello__world__test", "_", false));
        System.out.println(convertToCamelCase("_hello_world__test_", "_", true));
        System.out.println(convertToCamelCase("hello-world-test-", "-", false));
        System.out.println(convertToCamelCase("hello-world-test-", "-", true));

    }

}
