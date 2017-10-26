package cc.sportsdb.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public final class ToolUtil {

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
}
