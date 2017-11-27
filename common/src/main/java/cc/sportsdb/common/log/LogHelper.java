package cc.sportsdb.common.log;

import org.slf4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.springframework.http.MediaType.*;

public final class LogHelper {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<MediaType> LOG_REQUEST_BODY_MEDIA_TYPE = Arrays.asList(APPLICATION_JSON, APPLICATION_JSON_UTF8, APPLICATION_FORM_URLENCODED, APPLICATION_XML, TEXT_PLAIN, TEXT_XML);
    private static final List<MediaType> LOG_RESPONSE_BODY_MEDIA_TYPE = Arrays.asList(APPLICATION_JSON, APPLICATION_JSON_UTF8);

    private LogHelper() {
    }

    static boolean logIfNecessary(String url, LoggingProperties loggingProperties, Logger logger) {
        return loggingProperties.getLogLevel() != LogLevel.NONE && logger.isInfoEnabled()
                && !matchSuffix(url, loggingProperties.getIgnoreSuffixSet()) && !matchUrl(url, loggingProperties.getIgnoreUrlSet());
    }

    static boolean isLogResponseBody(String contentType) {
        try {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            return LOG_RESPONSE_BODY_MEDIA_TYPE.stream().anyMatch(mediaType::includes);
        } catch (Exception e) {
            return false;
        }
    }

    static HttpServletResponse logResponseBodyIfNecessary(HttpServletRequest request, HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return response;
        }

        if (isSpecialHttpMethod(request.getMethod())) {
            return response;
        }

        return new ContentCachingResponseWrapper(response);
    }

    static HttpServletRequest logRequestBodyIfNecessary(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return request;
        }

        if (isSpecialHttpMethod(request.getMethod())) {
            return request;
        }

        try {
            MediaType mediaType = MediaType.parseMediaType(request.getContentType());
            return LOG_REQUEST_BODY_MEDIA_TYPE.stream().noneMatch(mediaType::includes)
                    ? request
                    : new ContentCachingRequestWrapper(request);
        } catch (Exception e) {
            return request;
        }
    }

    private static boolean isSpecialHttpMethod(String httpMethod) {
        return !(httpMethod.equalsIgnoreCase(HttpMethod.POST.name())
                || httpMethod.equalsIgnoreCase(HttpMethod.PUT.name())
                || httpMethod.equalsIgnoreCase(HttpMethod.DELETE.name()));
    }

    private static boolean matchUrl(String url, Set<String> ignoreUrlSet) {
        return ignoreUrlSet.stream().anyMatch((ignoreUrl) -> PATH_MATCHER.match(ignoreUrl, url));
    }

    private static boolean matchSuffix(String url, Set<String> ignoreSuffixSet) {
        return ignoreSuffixSet.stream().anyMatch(url::endsWith);
    }

}
