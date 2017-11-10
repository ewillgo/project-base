package cc.sportsdb.common.log;

import cc.sportsdb.common.config.LoggingProperties;
import org.slf4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.springframework.http.MediaType.*;

public final class LogHelper {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<MediaType> LOG_BODY_MEDIA_TYPE = Arrays.asList(APPLICATION_JSON, APPLICATION_JSON_UTF8, APPLICATION_FORM_URLENCODED, APPLICATION_XML, TEXT_PLAIN, TEXT_XML);

    private LogHelper() {
    }

    public static boolean logIfNecessary(String url, LoggingProperties loggingProperties, Logger logger) {
        return loggingProperties.getLogLevel() != LogLevel.NONE && logger.isInfoEnabled()
                && !matchSuffix(url, loggingProperties.getIgnoreSuffixSet()) && !matchUrl(url, loggingProperties.getIgnoreUrlSet());
    }

    public static boolean cacheRequestIfNecessary(HttpServletRequest request, boolean isAsyncDispatch) {
        String httpMethod = request.getMethod();

        if (request instanceof ContentCachingRequestWrapper) {
            return false;
        }

        if (isAsyncDispatch
                || (!httpMethod.equalsIgnoreCase(HttpMethod.POST.name())
                && !httpMethod.equalsIgnoreCase(HttpMethod.PUT.name())
                && !httpMethod.equalsIgnoreCase(HttpMethod.DELETE.name()))) {
            return false;
        }

        try {
            MediaType mediaType = MediaType.parseMediaType(request.getContentType());
            return !LOG_BODY_MEDIA_TYPE.stream().noneMatch(mediaType::includes);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean matchUrl(String url, Set<String> ignoreUrlSet) {
        return ignoreUrlSet.stream().anyMatch((ignoreUrl) -> PATH_MATCHER.match(ignoreUrl, url));
    }

    private static boolean matchSuffix(String url, Set<String> ignoreSuffixSet) {
        return ignoreSuffixSet.stream().anyMatch(url::endsWith);
    }

}
