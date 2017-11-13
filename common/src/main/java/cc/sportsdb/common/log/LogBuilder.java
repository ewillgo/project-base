package cc.sportsdb.common.log;

import cc.sportsdb.common.util.JsonUtil;
import cc.sportsdb.common.util.ToolUtil;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LogBuilder {

    private final LogLevel logLevel;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private long startTime;
    private long endTime;

    LogBuilder(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    HttpServletRequest getHttpServletRequest() {
        return request;
    }

    LogBuilder setHttpServletRequest(HttpServletRequest request, boolean isAsyncDispatch) {
        this.request = LogHelper.logRequestBodyIfNecessary(request, isAsyncDispatch);
        return this;
    }

    HttpServletResponse getHttpServletResponse() {
        return response;
    }

    LogBuilder setHttpServletResponse(HttpServletResponse response, boolean isAsyncStarted) {
        if (request == null) {
            throw new RuntimeException("Request was null");
        }
        this.response = LogHelper.logResponseDataIfNecessary(request, response, isAsyncStarted);
        return this;
    }

    String buildRequestLog() {
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("url", getUrlWithQueryString());
        logMap.put("ip", ToolUtil.getRemoteIp(request));
        logMap.put("method", request.getMethod().toLowerCase());
        logMap.put("parameters", getParameterMap());
        logMap.put("body", getRequestBody());
        logMap.put("headers", getRequestHeaders());
        return String.format("{\"request\":%s}", JsonUtil.toJsonString(logMap));
    }

    String buildResponseLog() {
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("status", response.getStatus());
        logMap.put("spend", getSpendTime());
        logMap.put("url", getUrlWithQueryString());
        logMap.put("headers", getResponseHeaders());
        logMap.put("data", getResponseData());
        return String.format("{\"response\":%s}", JsonUtil.toJsonString(logMap));
    }

    LogBuilder setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    LogBuilder setEndTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    private Map<String, String[]> getParameterMap() {
        Map<String, String[]> paramMap = request.getParameterMap();
        return paramMap.size() > 0 ? paramMap : null;
    }

    private boolean isLogRequestBody() {
        return request instanceof ContentCachingRequestWrapper;
    }

    private boolean isLogResponseBody() {
        return response instanceof ContentCachingResponseWrapper;
    }

    private String getSpendTime() {
        return String.format("%.1fms", (endTime - startTime) / 1e6d);
    }

    private Object getRequestBody() {
        if (!isLogRequestBody()) {
            return null;
        }

        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);

        try {
            String requestBody = wrapper.getReader().lines().collect(Collectors.joining());
            if (!"".equals(requestBody)) {
                return JsonUtil.parse(requestBody, Map.class);
            }
        } catch (Exception e) {
        }

        return null;
    }

    private Object getResponseData() {
        if (!isLogResponseBody()) {
            return null;
        }

        String responseData = null;
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        byte[] bytes = wrapper.getContentAsByteArray();

        try {
            responseData = new String(bytes, 0, bytes.length, wrapper.getCharacterEncoding());
            if (!"".equals(responseData)) {
                return JsonUtil.parse(responseData, Map.class);
            }
        } catch (Exception e) {

        } finally {
            try {
                wrapper.copyBodyToResponse();
            } catch (IOException e) {
            }
        }

        return responseData;
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

    private Map<String, String> getResponseHeaders() {
        Map<String, String> headerMap = new LinkedHashMap<>();
        response.getHeaderNames().forEach((name) -> {
            headerMap.put(name, response.getHeader(name));
        });
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
