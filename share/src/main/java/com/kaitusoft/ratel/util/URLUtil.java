package com.kaitusoft.ratel.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/10
 *          <p>
 *          write description here
 */
public class URLUtil {

    public static final String REG_WC = ":[a-zA-Z0-9_]{1,}";
    public static final String REG_REG = "(\\^.*?\\$)|(\\^.*?)|(.*?\\$)|(.*([\\s\\S]).*)|(.*(\\b)|(\\*)|(\\\\[BcdDfnrsStvwWxp<>?.|()^&+]).*)";
    public static final String REG_PATH = "^(?:\\/([^?#]*))?(?:\\?([^#]*))?(?:#(.*))?";
    private static final Pattern PATTERN_WC = Pattern.compile(REG_WC);
    private static final Pattern PATTERN_REG = Pattern.compile(REG_REG);
    private static final Pattern PATTERN_PATH = Pattern.compile(REG_PATH);

    public static boolean hostMatch(String realHost, final Collection<String> hostDefined) {
        return true;
    }

    public static String graft(String origin, String queryString) {
        if (StringUtils.isEmpty(queryString))
            return origin;

        StringBuilder sb = new StringBuilder(origin);
        int paramStart = origin.indexOf('?');
        if (paramStart == -1) {
            sb.append('?');
        } else if (paramStart == (origin.length() - 1)) {
            sb.append('&');
        }
        sb.append(queryString.substring(1));
        return sb.toString();
    }

    public static String graft(String rule, String routePath, String requestPath, String queryString) {

        int prefixLength = 0;
        if (routePath != null)
            prefixLength = routePath.length();
        else {
            prefixLength = rule.length();
        }

        StringBuilder sb = new StringBuilder(requestPath.substring(prefixLength));
        if (StringUtils.isEmpty(queryString))
            return sb.toString();
        int paramStart = sb.indexOf("?");
        if (paramStart == -1) {
            sb.append('?');
        } else if (paramStart == (sb.length() - 1)) {
            sb.append('&');
        }
        sb.append(queryString);
        return sb.toString();
    }

    public static boolean isWildcard(String str) {
        return str.split(REG_WC).length > 1;
    }

    public static Set<String> getWildcard(String str) {
        Matcher m = PATTERN_WC.matcher(str);
        Set<String> wildcards = new HashSet<>();
        while (m.find()) {
            wildcards.add(m.group());
        }
        return wildcards;
    }

    public static boolean isCapture(String str) {
        int lnum = 0;
        int rnum = 0;
        int lindex = 0;
        int rindex = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '(') {
                lnum++;
                if (lindex == 0)
                    lindex = i;
            }

            if (ch == ')') {
                rnum++;
                if (rindex < i)
                    rindex = i;
            }
        }

        return lnum > 0 && lnum == rnum && rindex - lindex >= 0;
    }

    public static boolean isRegex(String str) {
        Matcher m = PATTERN_REG.matcher(str);
        return m.matches();
    }

    public static boolean isRegex2(String str) {
        return str.indexOf('^') >= 0 || str.indexOf('$') >= 0
                || str.indexOf('\\') >= 0 || str.indexOf('(') >= 0
                || str.indexOf('[') >= 0 || str.indexOf('~') >= 0;
    }

    public static boolean useful(String uri) throws IOException {
        return useful(new URL(uri));
    }

    public static boolean useful(URL url) {
        InputStream in = null;
        try {
            in = url.openStream();
            return true;
        } catch (Exception e) {

        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return false;
    }

    public static boolean validPath(String str) {
        return PATTERN_PATH.matcher(str).matches();
    }

    public static void main(String[] args) {
        System.out.println("/test*.jpg".replaceAll("\\*", ""));

        System.out.println(validPath("/[a-z]"));

        Pattern comm = Pattern.compile("abcd");
        Pattern reg = Pattern.compile("^[a-z]9$");

    }

}
