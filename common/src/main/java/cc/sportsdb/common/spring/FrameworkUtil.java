package cc.sportsdb.common.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.stream.Collectors;

public abstract class FrameworkUtil {

    private static final Logger logger = LoggerFactory.getLogger(FrameworkUtil.class);

    private FrameworkUtil() {
    }

    public static RestTemplate enhanceRestTemplate(RestTemplate restTemplate) {
        if (restTemplate == null) {
            throw new IllegalArgumentException("RestTemplate could not be null.");
        }

        restTemplate.getMessageConverters().removeIf(converter ->
                !(converter instanceof MappingJackson2HttpMessageConverter) &&
                        !(converter instanceof ByteArrayHttpMessageConverter) &&
                        !(converter instanceof MappingJackson2XmlHttpMessageConverter));

        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));

        logger.info("Message converters: {}", restTemplate.getMessageConverters().stream().map(c -> c.getClass().getSimpleName()).collect(Collectors.joining(",")));
        return restTemplate;
    }
}
