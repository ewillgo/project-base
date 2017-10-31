package cc.sportsdb.common.log.interceptor;

import cc.sportsdb.common.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebMvcMethodLogInterceptor extends HandlerInterceptorAdapter {

    private String profile;
    private static final Logger logger = LoggerFactory.getLogger(WebMvcMethodLogInterceptor.class);

    public WebMvcMethodLogInterceptor(String profile) {
        this.profile = profile;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod) || !"dev".equalsIgnoreCase(profile)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        logger.info("\n--------------------\nDate\t\t: {}\nUrl\t\t\t: {}\nMethod\t\t: {} {} {}.{}({});\nParameters\t: {}\n",
                DateUtil.now(),
                request.getRequestURL().toString(),
                getMethodAnnotations(handlerMethod),
                handlerMethod.getMethod().getReturnType().getSimpleName(),
                handlerMethod.getBeanType().getName(),
                handlerMethod.getMethod().getName(),
                getMethodParams(handlerMethod),
                getRequestParams(request));

        return true;
    }

    /**
     * Get invoke method annotations
     *
     * @param handlerMethod method object
     * @return Annotations string
     */
    private String getMethodAnnotations(HandlerMethod handlerMethod) {
        List<String> annotationList = new ArrayList<>();
        Annotation[] annotations = handlerMethod.getMethod().getAnnotations();
        for (Annotation annotation : annotations) {
            annotationList.add("@" + annotation.annotationType().getSimpleName());
        }
        return StringUtils.join(annotationList, " ");
    }

    /**
     * Get http request parameters
     *
     * @param request request object
     * @return Parameters string
     */
    private String getRequestParams(HttpServletRequest request) {
        List<String> requestParamList = new ArrayList<>();
        Map<String, String[]> map = request.getParameterMap();

        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            requestParamList.add(entry.getKey() + "=" + StringUtils.join(entry.getValue(), ","));
        }

        return StringUtils.join(requestParamList, ", ");
    }

    /**
     * Get invoke method parameters
     *
     * @param handlerMethod method object
     * @return Parameters string
     */
    private String getMethodParams(HandlerMethod handlerMethod) {
        String params = "";
        List<String> paramList = new ArrayList<>();

        Parameter[] parameters = handlerMethod.getMethod().getParameters();
        for (Parameter parameter : parameters) {
            for (Annotation annotation : parameter.getAnnotations()) {
                params += "@" + annotation.annotationType().getSimpleName() + " ";
            }
            params += parameter.getType().getSimpleName();
            paramList.add(params);
            params = "";
        }

        return StringUtils.join(paramList, ", ");
    }
}
