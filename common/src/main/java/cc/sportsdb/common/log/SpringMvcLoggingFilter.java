package cc.sportsdb.common.log;

import cc.sportsdb.common.config.LoggingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SpringMvcLoggingFilter extends OncePerRequestFilter implements Ordered {

    private final LoggingProperties loggingProperties;
    private static final Logger logger = LoggerFactory.getLogger(SpringMvcLoggingFilter.class);

    public SpringMvcLoggingFilter(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!LogHelper.logIfNecessary(request.getRequestURL().toString(), loggingProperties, logger)) {
            filterChain.doFilter(request, response);
            return;
        }

    }

    @Override
    public int getOrder() {
        return -1;
    }
}
