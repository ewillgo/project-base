package cc.sportsdb.common.http;

import cc.sportsdb.common.log.HttpClientInterceptor;
import cc.sportsdb.common.spring.FrameworkUtil;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static cc.sportsdb.common.http.RestConstant.NAME;
import static cc.sportsdb.common.http.RestConstant.RAW_NAME;

@Configuration
@Import(Okhttp3Properties.class)
public class RestTemplateConfig {

    @Autowired
    private Okhttp3Properties okhttp3Properties;

    @Bean
    @Primary
    @LoadBalanced
    public RestTemplate restTemplate(OkHttp3ClientHttpRequestFactory factory,
                                     MappingJackson2HttpMessageConverter jsonConverter) {
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setMessageConverters(Collections.singletonList(jsonConverter));
        return FrameworkUtil.enhanceRestTemplate(NAME, restTemplate);
    }

    @Bean(name = RAW_NAME)
    public RestTemplate httpRestTemplate(OkHttp3ClientHttpRequestFactory factory,
                                         MappingJackson2HttpMessageConverter jsonConverter,
                                         StringHttpMessageConverter stringConverter) {
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setMessageConverters(Arrays.asList(jsonConverter, stringConverter));
        return FrameworkUtil.enhanceRestTemplate(RAW_NAME, restTemplate);
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new HttpClientInterceptor())
                .followRedirects(true)
                .followSslRedirects(true)
                .readTimeout(okhttp3Properties.getReadTimeout(), TimeUnit.SECONDS)
                .connectTimeout(okhttp3Properties.getConnectTimeout(), TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(okhttp3Properties.getMaxIdleConnections(), okhttp3Properties.getKeepAliveDuration(), TimeUnit.MINUTES))
                .build();
    }

    @Bean
    public OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory(OkHttpClient okHttpClient) {
        return new OkHttp3ClientHttpRequestFactory(okHttpClient);
    }
}
