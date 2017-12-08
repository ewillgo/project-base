package cc.sportsdb.common.util;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.UUID;

public abstract class ToolUtil {

    private static final String CHARSET = "UTF-8";

    private ToolUtil() {
    }

    public static String encodeUrl(String url) {
        String encodeUrl = null;
        try {
            encodeUrl = URLEncoder.encode(url, CHARSET);
        } catch (UnsupportedEncodingException ignore) {
        }
        return encodeUrl;
    }

    public static String decodeUrl(String url) {
        String decodeUrl = null;
        try {
            decodeUrl = URLDecoder.decode(url, CHARSET);
        } catch (UnsupportedEncodingException ignore) {
        }
        return decodeUrl;
    }

    public static String getRemoteIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }
}
