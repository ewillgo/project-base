package cc.sportsdb.common.log.aspect;

import cc.sportsdb.common.util.DateUtil;
import cc.sportsdb.common.util.JsonUtil;
import cc.sportsdb.common.util.ToolUtil;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Aspect
@Component
public class WebLogAspect implements Ordered {

    @Value("${spring.cloud.config.profile}")
    private String profile;

    private static ThreadLocal<Long> startTime = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(WebLogAspect.class);

    @Value("9")
    private int order;

    @Pointcut("execution(public * cc.sportsdb..*.controller..*.*(..))")
    public void pointcut() {
    }

    @Before(value = "pointcut()")
    public void before(JoinPoint joinPoint) {

        if (!"dev".equals(profile)) {
            return;
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        logger.info("\nWeb Request --------------------\nDate\t\t: {}\nRemote Ip\t: {}\nRequest\t\t: {} {}\nMethod\t\t: {}\nParameters\t: {}\n",
                DateUtil.now(),
                ToolUtil.getRemoteIp(request),
                request.getMethod(),
                request.getRequestURL().toString() + getQueryString(request),
                getMethodString(joinPoint),
                getRequestParams(request)
        );

        startTime.set(System.nanoTime());
    }

    @AfterReturning(returning = "result", pointcut = "pointcut()")
    public void after(JoinPoint joinPoint, Object result) {

        if (!"dev".equals(profile)) {
            return;
        }

        String responseString = "";
        try {
            responseString = JsonUtil.toJsonString(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        logger.info("\nWeb Response --------------------\nDate\t\t: {}\nSpend\t\t: {}\nRequest\t\t: {} {}\nMethod\t\t: {}\nParameters\t: {}\nResponse\t: {}\n",
                DateUtil.now(),
                String.format("%.1fms", (System.nanoTime() - startTime.get()) / 1e6d),
                request.getMethod(),
                request.getRequestURL().toString() + getQueryString(request),
                getMethodString(joinPoint),
                getRequestParams(request),
                responseString
        );
    }

    /**
     * Get method string
     *
     * @param joinPoint object
     * @return Method string
     */
    private String getMethodString(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        Method[] methods = signature.getDeclaringType().getMethods();

        String methodString = "";
        for (Method method : methods) {
            if (joinPoint.getSignature().getName().equals(method.getName())) {
                methodString += getAnnotationString(method.getAnnotations()) + signature.getDeclaringTypeName() + "." + signature.getName() + "(" + getParameterString(method.getParameters()) + ");";
                break;
            }
        }

        return methodString;
    }

    /**
     * Get http request parameters
     *
     * @param request Request object
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
     * Get parameter string
     *
     * @param parameters Parameter object
     * @return Parameter string
     */
    private String getParameterString(Parameter[] parameters) {
        String parameterString = "";
        if (parameters != null && parameters.length > 0) {
            for (Parameter parameter : parameters) {
                parameterString += (parameterString.length() != 0 ? ", " : "") + getAnnotationString(parameter.getAnnotations()) + parameter.getType().getSimpleName();
            }
        }
        return parameterString;
    }

    /**
     * Get annotation string
     *
     * @param annotations Annotation object
     * @return Annotation string
     */
    private String getAnnotationString(Annotation[] annotations) {
        String annotationString = "";
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                annotationString += "@" + annotation.annotationType().getSimpleName() + " ";
            }
        }
        return annotationString;
    }

    /**
     * Get http request query string
     *
     * @param request Request object
     * @return Query string
     */
    private String getQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return queryString != null ? "?" + queryString : "";
    }

    @Override
    public int getOrder() {
        return order;
    }
}
