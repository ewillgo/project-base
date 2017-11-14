package cc.sportsdb.common.log;

import cc.sportsdb.common.util.JsonUtil;
import okhttp3.*;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpClientInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientInterceptor.class);

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Map<String, Object> requestMap = new LinkedHashMap<>();
        requestMap.put("url", request.url().url().toString());
        requestMap.put("method", request.method().toLowerCase());
        requestMap.put("parameters", getQueryString(request));
        requestMap.put("body", requestBodyToMap(requestBodyToString(request)));
        requestMap.put("headers", request.headers().toMultimap());
        logger.info("ok3req:{}", JsonUtil.toJsonString(requestMap));

        long t1 = System.nanoTime();
        Response response = chain.proceed(chain.request());
        long t2 = System.nanoTime();

        MediaType mediaType = response.body().contentType();
        String responseString = getResponseString(response);
        Map<String, Object> responseMap = new LinkedHashMap<>();
        responseMap.put("status", response.code());
        responseMap.put("time", String.format("%.1fms", (t2 - t1) / 1e6d));
        responseMap.put("url", response.request().url().toString());
        responseMap.put("headers", response.headers().toMultimap());
        responseMap.put("respdata", getResponseMap(mediaType, responseString));
        logger.info("ok3resp:{}", JsonUtil.toJsonString(responseMap));

        return response.newBuilder()
                .body(ResponseBody.create(mediaType, responseString))
                .build();
    }

    private Map<String, List<String>> getQueryString(Request request) {
        HttpUrl url = request.url();
        Set<String> nameSet = url.queryParameterNames();

        if (nameSet == null) {
            return null;
        }

        Map<String, List<String>> paramMap = new LinkedHashMap<>();
        nameSet.forEach((name) -> {
            paramMap.put(name, url.queryParameterValues(name));
        });

        return paramMap.isEmpty() ? null : paramMap;
    }

    private static final String JSON_TYPE = "application/json";

    private String getResponseString(Response response) {
        String responseString = "";

        try {
            responseString = response.body().string();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return responseString;
    }

    private Map<String, Object> getResponseMap(MediaType mediaType, String responseString) {
        Map<String, Object> responseMap = null;

        if (JSON_TYPE.equalsIgnoreCase(String.format("%s/%s", mediaType.type(), mediaType.subtype()))) {
            try {
                responseMap = JsonUtil.parse(responseString, Map.class);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return responseMap;
    }

    private Map<String, Object> requestBodyToMap(String requestBodyString) {
        Map<String, Object> requestBodyMap = null;
        try {
            if (requestBodyString != null) {
                requestBodyMap = JsonUtil.parse(requestBodyString, Map.class);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return requestBodyMap;
    }

    private String requestBodyToString(Request request) {
        String bodyString = null;
        if (request.body() != null) {
            final Buffer buffer = new Buffer();
            try {
                request.newBuilder().build().body().writeTo(buffer);
                bodyString = buffer.readUtf8();
            } catch (IOException ignore) {
            }
        }

        return bodyString;
    }
}
