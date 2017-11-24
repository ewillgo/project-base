package cc.sportsdb.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Component
public final class HttpUtil implements ApplicationContextAware {

    private static RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private HttpUtil() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        restTemplate = applicationContext.getBean("httpRestTemplate", RestTemplate.class);
    }

    public static <T> T httpHead(String url, Class<T> responseType) {
        return invoke(url, HttpMethod.HEAD, responseType, null, null, null);
    }

    public static <T> T httpHead(String url, Class<T> responseType, Map<String, ?> data) {
        return invoke(url, HttpMethod.HEAD, responseType, data, null, null);
    }

    public static <T> T httpHead(String url, Class<T> responseType, HttpHeaders httpHeaders) {
        return invoke(url, HttpMethod.HEAD, responseType, null, httpHeaders, null);
    }

    public static <T> T httpHead(String url, Class<T> responseType, Map<String, ?> data, HttpHeaders httpHeaders) {
        return invoke(url, HttpMethod.HEAD, responseType, data, httpHeaders, null);
    }

    public static <T> T httpDelete(String url, Class<T> responseType) {
        return invoke(url, HttpMethod.DELETE, responseType, null, null, null);
    }

    public static <T> T httpDelete(String url, Class<T> responseType, Map<String, ?> data) {
        return invoke(url, HttpMethod.DELETE, responseType, data, null, null);
    }

    public static <T> T httpDelete(String url, Class<T> responseType, HttpHeaders httpHeaders) {
        return invoke(url, HttpMethod.DELETE, responseType, null, httpHeaders, null);
    }

    public static <T> T httpDelete(String url, Class<T> responseType, Map<String, ?> data, HttpHeaders httpHeaders) {
        return invoke(url, HttpMethod.DELETE, responseType, data, httpHeaders, null);
    }

    public static <T> T httpPut(String url, Class<T> responseType) {
        return invoke(url, HttpMethod.PUT, responseType, null, null, null);
    }

    public static <T> T httpPut(String url, Class<T> responseType, Map<String, ?> data) {
        return invoke(url, HttpMethod.PUT, responseType, data, null, null);
    }

    public static <T> T httpPut(String url, Class<T> responseType, HttpHeaders httpHeaders) {
        return invoke(url, HttpMethod.PUT, responseType, null, httpHeaders, null);
    }

    public static <T> T httpPut(String url, Class<T> responseType, Map<String, ?> data, HttpHeaders httpHeaders) {
        return invoke(url, HttpMethod.PUT, responseType, data, httpHeaders, null);
    }

    public static <T> T httpGet(String url, Class<T> responseType) {
        return invoke(url, HttpMethod.GET, responseType, null, null, null);
    }

    public static <T> T httpGet(String url, Class<T> responseType, Map<String, ?> data) {
        return invoke(url, HttpMethod.GET, responseType, data, null, null);
    }

    public static <T> T httpGet(String url, Class<T> responseType, HttpHeaders httpHeaders) {
        return invoke(url, HttpMethod.GET, responseType, null, httpHeaders, null);
    }

    public static <T> T httpGet(String url, Class<T> responseType, Map<String, ?> data, HttpHeaders httpHeaders) {
        return invoke(url, HttpMethod.GET, responseType, data, httpHeaders, null);
    }

    public static <R, T> R httpPost(String url, Class<R> responseType) {
        return invoke(url, HttpMethod.POST, responseType, null, null, null);
    }

    public static <R, T> R httpPost(String url, Class<R> responseType, Map<String, ?> data) {
        return invoke(url, HttpMethod.POST, responseType, data, null, null);
    }

    public static <R, T> R httpPost(String url, Class<R> responseType, Map<String, ?> data, HttpHeaders httpHeaders) {
        return invoke(url, HttpMethod.POST, responseType, data, httpHeaders, null);
    }

    public static <R, T> R httpPost(String url, Class<R> responseType, Map<String, ?> data, T requestBody) {
        return invoke(url, HttpMethod.POST, responseType, data, null, requestBody);
    }

    public static <R, T> R httpPost(String url, Class<R> responseType, HttpHeaders httpHeaders, T requestBody) {
        return invoke(url, HttpMethod.POST, responseType, null, httpHeaders, requestBody);
    }

    public static <R, T> R httpPost(String url, Class<R> responseType, T requestBody) {
        return invoke(url, HttpMethod.POST, responseType, null, null, requestBody);
    }

    public static <R, T> R httpPost(String url, Class<R> responseType, HttpHeaders httpHeaders) {
        return invoke(url, HttpMethod.POST, responseType, null, httpHeaders, null);
    }

    public static <R, T> R httpPost(String url, Class<R> responseType, Map<String, ?> data, HttpHeaders httpHeaders, T requestBody) {
        return invoke(url, HttpMethod.POST, responseType, data, httpHeaders, requestBody);
    }

    private static <R, T> R invoke(String url, HttpMethod httpMethod, Class<R> responseType, Map<String, ?> data, HttpHeaders httpHeaders, T requestBody) {
        ResponseEntity<R> responseEntity = null;

        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Empty url.");
        }

        try {
            responseEntity = data != null
                    ? restTemplate.exchange(urlWithQueryString(url, data), httpMethod, new HttpEntity<>(requestBody, httpHeaders), responseType, data)
                    : restTemplate.exchange(new URI(urlWithQueryString(url, data)), httpMethod, new HttpEntity<>(requestBody, httpHeaders), responseType);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        }

        return null;
    }

    private static String urlWithQueryString(String url, Map<String, ?> data) {

        if (data == null || data.isEmpty()) {
            return url;
        }

        StringBuilder sb = new StringBuilder();
        data.forEach((k, v) -> {
            sb.append((url.lastIndexOf("?") < 0 && sb.length() == 0) ? "?" : "&").append(k).append("=").append("{").append(k).append("}");
        });

        sb.insert(0, url);
        return sb.toString();
    }
}
