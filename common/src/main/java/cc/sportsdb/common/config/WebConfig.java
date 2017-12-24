package cc.sportsdb.common.config;

import cc.sportsdb.common.data.mq.MqConfig;
import cc.sportsdb.common.data.redis.RedisConfig;
import cc.sportsdb.common.database.DataSourceConfig;
import cc.sportsdb.common.http.RestTemplateConfig;
import cc.sportsdb.common.log.LoggingProperties;
import cc.sportsdb.common.log.SpringMvcLoggingFilter;
import cc.sportsdb.common.spring.ApplicationContextHolder;
import cc.sportsdb.common.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;

@Configuration
@EnableAsync
@ImportAutoConfiguration({
        WebMvcConfig.class,
        DataSourceConfig.class,
        RestTemplateConfig.class,
        RedisConfig.class,
        MqConfig.class
})
public class WebConfig implements ApplicationContextAware {

    @Value("${spring.cloud.config.profile:prod}")
    private String profile;

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return JsonUtil.OBJECT_MAPPER;
    }

    @Bean
    @ConditionalOnMissingBean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        converter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON_UTF8
        ));
        return converter;
    }

    @Bean
    @ConditionalOnMissingBean
    public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
        return new ByteArrayHttpMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter() {
        return new MappingJackson2XmlHttpMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingProperties loggingProperties() {
        return new LoggingProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringMvcLoggingFilter springMvcLoggingFilter(LoggingProperties loggingProperties) {
        loggingProperties.setLogLevel(profile);
        return new SpringMvcLoggingFilter(loggingProperties);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.setApplicationContext(applicationContext);
    }
}
