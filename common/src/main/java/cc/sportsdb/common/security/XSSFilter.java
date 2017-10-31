package cc.sportsdb.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(filterName = "xssFilter", urlPatterns = "/*", asyncSupported = true)
public class XSSFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(XSSFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("XSSFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(new XSSHttpServletRequest((HttpServletRequest) servletRequest), servletResponse);
    }

    @Override
    public void destroy() {

    }
}
