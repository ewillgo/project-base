package cc.sportsdb.common.config;

import cc.sportsdb.common.log.interceptor.WebMvcMethodLogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Autowired(required = false)
    private HandlerInterceptorList handlerInterceptorList;

    @Value("${spring.cloud.config.profile}")
    private String profile;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new WebMvcMethodLogInterceptor(profile));
        if (handlerInterceptorList != null) {
            for (HandlerInterceptor handlerInterceptor : handlerInterceptorList.getHandlerInterceptors()) {
                registry.addInterceptor(handlerInterceptor);
            }
            for (WebRequestInterceptor webRequestInterceptor : handlerInterceptorList.getWebRequestInterceptors()) {
                registry.addWebRequestInterceptor(webRequestInterceptor);
            }
        }
    }
}
