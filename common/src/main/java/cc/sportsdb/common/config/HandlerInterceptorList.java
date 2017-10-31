package cc.sportsdb.common.config;

import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandlerInterceptorList {
    private List<HandlerInterceptor> handlerInterceptorList = new ArrayList<>();
    private List<WebRequestInterceptor> webRequestInterceptorList = new ArrayList<>();

    public void addHandlerInterceptors(HandlerInterceptor... handlerInterceptors) {
        if (handlerInterceptors.length > 0) {
            handlerInterceptorList.addAll(Arrays.asList(handlerInterceptors));
        }
    }

    public void addWebRequestInterceptors(WebRequestInterceptor... webRequestInterceptors) {
        if (webRequestInterceptors.length > 0) {
            webRequestInterceptorList.addAll(Arrays.asList(webRequestInterceptors));
        }
    }

    public List<HandlerInterceptor> getHandlerInterceptors() {
        return handlerInterceptorList;
    }

    public List<WebRequestInterceptor> getWebRequestInterceptors() {
        return webRequestInterceptorList;
    }
}
