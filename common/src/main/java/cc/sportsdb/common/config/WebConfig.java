package cc.sportsdb.common.config;

import cc.sportsdb.common.database.config.DataSourceConfig;
import cc.sportsdb.common.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
@ComponentScan(value = {"cc.sportsdb.common.**.controller", "cc.sportsdb.common.log.**.aspect", "cc.sportsdb.common.**.util"})
@ImportAutoConfiguration({WebMvcConfig.class, DataSourceConfig.class, RestTemplateConfig.class})
public class WebConfig {

    @Bean
    @Primary
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter(JsonUtil.OBJECT_MAPPER);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonUtil.OBJECT_MAPPER;
    }

}
