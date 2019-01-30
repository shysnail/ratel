package com.kaitusoft.ratel.util;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/19
 *          <p>
 *          write description here
 */
public class StringUtils {

    private static final long MILL_MINUTE = 60000;

    private static final long MILL_HOUR = 60 * MILL_MINUTE;

    private static final long MILL_DAY = 24 * MILL_HOUR;
    private static final String regIpv4 = "^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$";
    private static final Pattern PATTERN_IPV4 = Pattern.compile(regIpv4);
    private static final String regIpv6 = "(?i)^((([\\da-f]{1,4}:){7}[\\da-f]{1,4})|(([\\da-f]{1,4}:){1,7}:)|(([\\da-f]{1,4}:){6}:[\\da-f]{1,4})|(([\\da-f]{1,4}:){5}(:[\\da-f]{1,4}){1,2})|(([\\da-f]{1,4}:){4}(:[\\da-f]{1,4}){1,3})|(([\\da-f]{1,4}:){3}(:[\\da-f]{1,4}){1,4})|(([\\da-f]{1,4}:){2}(:[\\da-f]{1,4}){1,5})|([\\da-f]{1,4}:(:[\\da-f]{1,4}){1,6})|(:(:[\\da-f]{1,4}){1,7})|(([\\da-f]{1,4}:){6}(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([\\da-f]{1,4}:){5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([\\da-f]{1,4}:){4}(:[\\da-f]{1,4}){0,1}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([\\da-f]{1,4}:){3}(:[\\da-f]{1,4}){0,2}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([\\da-f]{1,4}:){2}(:[\\da-f]{1,4}){0,3}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|([\\da-f]{1,4}:(:[\\da-f]{1,4}){0,4}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(:(:[\\da-f]{1,4}){0,5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}))$";
    private static final Pattern PATTERN_IPV6 = Pattern.compile(regIpv6);
    private static SimpleDateFormat rfc822DateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

    static {
        rfc822DateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));
    }

    public static String toReadableTime(long millSeconds) {
        String result = "";
        if (millSeconds > MILL_DAY)
            result += (millSeconds / MILL_DAY) + "天 ";

        if (millSeconds > MILL_HOUR)
            result += ((millSeconds % MILL_DAY) / MILL_HOUR) + "小时 ";

        if (millSeconds > MILL_MINUTE)
            result += ((millSeconds % MILL_DAY % MILL_HOUR) / MILL_MINUTE) + "分钟 ";

        result += ((millSeconds % MILL_DAY % MILL_HOUR % MILL_MINUTE) / 1000) + "秒";

        return result;
    }

    public static String getRfc822DateFormat(Date date) {
        return rfc822DateFormat.format(date);
    }

    public static String uniqueId() {
        return UUID.randomUUID().toString();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNumric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;

        int start = chars[0] == '-' ? 1 : 0;
        if ((sz > start + 1) && (chars[start] == '0')
                && (chars[(start + 1)] == 'x')) {
            int i = start + 2;
            if (i == sz) {
                return false;
            }
            do {
                if (((chars[i] < '0') || (chars[i] > '9'))
                        && ((chars[i] < 'a') || (chars[i] > 'f'))
                        && ((chars[i] < 'A') || (chars[i] > 'F'))) {
                    return false;
                }
                i++;
            } while (i < chars.length);
            return true;
        }
        sz--;
        int i = start;
        while ((i < sz) || ((i < sz + 1) && (allowSigns) && (!foundDigit))) {
            if ((chars[i] >= '0') && (chars[i] <= '9')) {
                foundDigit = true;
                allowSigns = false;
            } else if (chars[i] == '.') {
                if ((hasDecPoint) || (hasExp)) {
                    return false;
                }
                hasDecPoint = true;
            } else if ((chars[i] == 'e') || (chars[i] == 'E')) {
                if (hasExp) {
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if ((chars[i] == '+') || (chars[i] == '-')) {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false;
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if ((chars[i] >= '0') && (chars[i] <= '9')) {
                return true;
            }
            if ((chars[i] == 'e') || (chars[i] == 'E')) {
                return false;
            }
            if (chars[i] == '.') {
                if ((hasDecPoint) || (hasExp)) {
                    return false;
                }
                return foundDigit;
            }
            if ((!allowSigns)
                    && ((chars[i] == 'd') || (chars[i] == 'D')
                    || (chars[i] == 'f') || (chars[i] == 'F'))) {
                return foundDigit;
            }
            if ((chars[i] == 'l') || (chars[i] == 'L')) {
                return (foundDigit) && (!hasExp) && (!hasDecPoint);
            }
            return false;
        }
        return (!allowSigns) && (foundDigit);
    }

    /**
     * 判断是不是ip，正则匹配，不很快
     *
     * @return
     */
    public static boolean isIp(String ip) {
        Matcher m = PATTERN_IPV4.matcher(ip);
        if (m.matches())
            return true;

        m = PATTERN_IPV6.matcher(ip);
        return m.matches();
    }

    /**
     * 首先，得是ip，这里不再判断
     *
     * @param ip
     * @return
     */
    public static boolean isIpV4(String ip) {
        if (isEmpty(ip))
            return false;

        return ip.indexOf('.') > 0;
    }

    public static boolean isIpV6(String ip) {
        if (isEmpty(ip))
            return false;

        return ip.indexOf(':') > 0;
    }

    public static boolean isMatch(String input, String ref) {
        String tp = "";

        //处理p中多余的*
        for (int i = 0; i < ref.length(); i++) {
            if (ref.charAt(i) == '*') {
                tp += '*';
                while (i < ref.length() && ref.charAt(i) == '*') i++;
            }
            if (i < ref.length()) {
                tp += ref.charAt(i);
            }
        }
        ref = tp;

        boolean[][] f = new boolean[input.length() + 1][ref.length() + 1];
        f[0][0] = true;

        // 注意，当p以*开头时
        if (ref.length() > 0 && ref.charAt(0) == '*') {
            f[0][1] = true;
        }

        for (int i = 1; i <= input.length(); i++) {
            for (int j = 1; j <= ref.length(); j++) {
                if (ref.charAt(j - 1) == '*') {
                    f[i][j] = f[i - 1][j - 1] || f[i - 1][j] || f[i][j - 1];
                } else {
                    f[i][j] = f[i - 1][j - 1] && (input.charAt(i - 1) == ref.charAt(j - 1) || ref.charAt(j - 1) == '?');
                }
            }
        }

        return f[input.length()][ref.length()];
    }

    private static final String CHAR = "abcdefghijklmnopqrstuvwxyz1234567890";
    public static String getRandomStr(int length) {
        char[] code = CHAR.toCharArray();
        StringBuffer str = new StringBuffer();
        Random r = new Random();

        for(int i = 0; i < length; ++i) {
            str.append(code[r.nextInt(code.length - 1)]);
        }

        return str.toString();
    }
}
