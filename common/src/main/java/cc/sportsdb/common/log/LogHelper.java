package cc.sportsdb.common.log;

import cc.sportsdb.common.config.LoggingProperties;
import org.slf4j.Logger;
import org.springframework.util.AntPathMatcher;

import java.util.Set;

public final class LogHelper {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private LogHelper() {
    }

    public static boolean logIfNecessary(String url, LoggingProperties loggingProperties, Logger logger) {
        return loggingProperties.getLogLevel() != LogLevel.NONE && logger.isInfoEnabled()
                && !matchSuffix(url, loggingProperties.getIgnoreSuffixSet()) && !matchUrl(url, loggingProperties.getIgnoreUrlSet());
    }

    private static boolean matchUrl(String url, Set<String> ignoreUrlSet) {
        return ignoreUrlSet.stream().anyMatch((ignoreUrl) -> PATH_MATCHER.match(ignoreUrl, url));
    }

    private static boolean matchSuffix(String url, Set<String> ignoreSuffixSet) {
        return ignoreSuffixSet.stream().anyMatch(url::endsWith);
    }

}
