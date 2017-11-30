package cc.sportsdb.common.config;

import cc.sportsdb.common.data.redis.RedisConfig;
import cc.sportsdb.common.database.config.DataSourceConfig;
import cc.sportsdb.common.http.RestTemplateConfig;
import cc.sportsdb.common.log.LoggingProperties;
import cc.sportsdb.common.log.SpringMvcLoggingFilter;
import cc.sportsdb.common.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

import java.util.Arrays;

@Configuration
@EnableCaching
@ComponentScan(value = {"cc.sportsdb.common.**.controller", "cc.sportsdb.common.**.log", "cc.sportsdb.common.**.util"})
@ImportAutoConfiguration({
        WebMvcConfig.class,
        DataSourceConfig.class,
        RestTemplateConfig.class,
        RedisConfig.class
})
public class WebConfig {

    @Value("${spring.cloud.config.profile:prod}")
    private String profile;

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonUtil.OBJECT_MAPPER;
    }

    @Bean
    @Primary
    @ConditionalOnBean(ObjectMapper.class)
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        converter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON_UTF8
        ));
        return converter;
    }

    @Bean
    public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
        return new ByteArrayHttpMessageConverter();
    }

    @Bean
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

}
