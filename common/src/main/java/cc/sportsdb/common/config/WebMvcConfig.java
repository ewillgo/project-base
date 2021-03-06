package cc.sportsdb.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Autowired(required = false)
    private HandlerInterceptorList handlerInterceptorList;

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.ignoreAcceptHeader(true)
                .useJaf(false)
                .mediaType("xml", MediaType.APPLICATION_XML)
                .mediaType("json", MediaType.APPLICATION_JSON);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
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
