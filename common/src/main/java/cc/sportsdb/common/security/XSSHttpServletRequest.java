package cc.sportsdb.common.security;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class XSSHttpServletRequest extends HttpServletRequestWrapper {

    private static final Whitelist whitelist = Whitelist.basicWithImages();

    XSSHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        return clean(super.getParameter(name));
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = new HashMap<>(super.getParameterMap());
        if (map == null || map.isEmpty()) {
            return map;
        }

        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String[] values = entry.getValue();
            if (values == null) {
                continue;
            }

            String[] newValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                newValues[i] = clean(values[i]);
            }

            map.put(entry.getKey(), newValues);
        }

        return Collections.unmodifiableMap(map);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);

        if (values == null) {
            return values;
        }

        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                values[i] = clean(values[i]);
            }
        }

        return values;
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        return Jsoup.clean(value, whitelist);
    }

}
