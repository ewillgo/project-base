package cc.sportsdb.common.config;

import cc.sportsdb.common.log.LogLevel;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LoggingProperties {
    private LogLevel logLevel;
    private Set<String> ignoreUrlSet = new HashSet<>();
    private Set<String> ignoreSuffixSet = new HashSet<>();

    public LoggingProperties() {
        init();
    }

    private void init() {
        ignoreUrlSet.addAll(Arrays.asList("/**/encrypt", "/**/decrypt", "/fonts/**", "/css/**"));
        ignoreSuffixSet.addAll(Arrays.asList(".jpg", ".js", ".json", ".png", ".tiff", ".bmp", ".gif", ".jpeg", ".ico", ".css"));
    }

    public LoggingProperties addIgnoreUrl(String url) {
        ignoreUrlSet.add(url);
        return this;
    }

    public LoggingProperties addIgnoreSuffix(String suffix) {
        ignoreSuffixSet.add(suffix);
        return this;
    }

    public Set<String> getIgnoreUrlSet() {
        return Collections.unmodifiableSet(ignoreUrlSet);
    }

    public Set<String> getIgnoreSuffixSet() {
        return Collections.unmodifiableSet(ignoreSuffixSet);
    }

    LoggingProperties setLogLevel(String level) {
        this.logLevel = LogLevel.valueOf(level.toUpperCase());
        return this;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public String toString() {
        return "LoggingProperties{" +
                "logLevel=" + logLevel +
                ", ignoreUrlSet=" + ignoreUrlSet +
                ", ignoreSuffixSet=" + ignoreSuffixSet +
                '}';
    }
}
