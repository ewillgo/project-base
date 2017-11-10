package cc.sportsdb.common.log;

import cc.sportsdb.common.util.JsonUtil;
import cc.sportsdb.common.util.ToolUtil;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

public class LogBuilder {

    private final LogLevel logLevel;
    private final HttpServletRequest request;

    LogBuilder(HttpServletRequest request, LogLevel logLevel) {
        this.request = request;
        this.logLevel = logLevel;
    }

    public String buildSpringMvcRequestLog() {
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("url", getUrlWithQueryString());
        logMap.put("ip", ToolUtil.getRemoteIp(request));
        logMap.put("method", request.getMethod());
        logMap.put("parameters", request.getParameterMap());
        logMap.put("headers", getRequestHeaders());
        logMap.put("loglevel", logLevel.name());
        return String.format("{\"request\":%s}", JsonUtil.toJsonString(logMap));
    }

    public String buildSpringMvcResponseLog(long startTime, long endTime) {
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("url", getUrlWithQueryString());
        logMap.put("loglevel", logLevel.name());
        logMap.put("logdata", getResponseData());
        return String.format("{\"response\":%s}", JsonUtil.toJsonString(logMap));
    }

    private Object getResponseData() {
        ContentCachingRequestWrapper wrapper =
                WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper == null) {
            return null;
        }

        String responseData = null;
        byte[] bytes = wrapper.getContentAsByteArray();

        try {
            responseData = new String(bytes, 0, bytes.length, wrapper.getCharacterEncoding());
            return JsonUtil.parse(responseData, Map.class);
        } catch (Exception e) {
            return responseData;
        }
    }

    private Map<String, String> getRequestHeaders() {
        Map<String, String> headerMap = new LinkedHashMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String headerName = enumeration.nextElement();
            headerMap.put(headerName, request.getHeader(headerName));
        }
        return headerMap.isEmpty() ? null : headerMap;
    }

    /**
     * Get parameter string
     *
     * @param parameters Parameter object
     * @return Parameter string
     */
    private String getParameterString(Parameter[] parameters) {
        String parameterString = "";
        if (parameters != null && parameters.length > 0) {
            for (Parameter parameter : parameters) {
                parameterString += (parameterString.length() != 0 ? ", " : "") + getAnnotationString(parameter.getAnnotations()) + parameter.getType().getSimpleName();
            }
        }
        return parameterString;
    }

    /**
     * Get annotation string
     *
     * @param annotations Annotation object
     * @return Annotation string
     */
    private String getAnnotationString(Annotation[] annotations) {
        String annotationString = "";
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                annotationString += "@" + annotation.annotationType().getSimpleName() + " ";
            }
        }
        return annotationString;
    }

    /**
     * Get http request url with query string
     *
     * @return Query string
     */
    private String getUrlWithQueryString() {
        String queryString = request.getQueryString();
        return ToolUtil.decodeUrl(request.getRequestURL().toString()) + (queryString != null ? "?" + queryString : "");
    }
}
