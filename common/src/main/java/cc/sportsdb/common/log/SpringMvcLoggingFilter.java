package cc.sportsdb.common.log;

import cc.sportsdb.common.config.LoggingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SpringMvcLoggingFilter extends OncePerRequestFilter implements Ordered {

    private final LoggingProperties loggingProperties;
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcLoggingFilter.class);

    public SpringMvcLoggingFilter(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!LogHelper.logIfNecessary(request.getRequestURL().toString(), loggingProperties, LOGGER)) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = 0;
        boolean logResponse = false;
        HttpServletRequest httpServletRequest = request;
        try {
            if (LogHelper.cacheRequestIfNecessary(request, isAsyncDispatch(request))) {
                httpServletRequest = new ContentCachingRequestWrapper(request);
                logResponse = true;
            }
            LOGGER.info(new LogBuilder(httpServletRequest, loggingProperties.getLogLevel()).buildSpringMvcRequestLog());
            startTime = System.nanoTime();
            filterChain.doFilter(httpServletRequest, response);
        } finally {
            if (logResponse && !isAsyncStarted(httpServletRequest)) {
                ContentCachingRequestWrapper wrapper =
                        WebUtils.getNativeRequest(httpServletRequest, ContentCachingRequestWrapper.class);
                long endTime = System.nanoTime();
                LOGGER.info(new LogBuilder(httpServletRequest, loggingProperties.getLogLevel()).buildSpringMvcResponseLog(startTime, endTime));
            }
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
