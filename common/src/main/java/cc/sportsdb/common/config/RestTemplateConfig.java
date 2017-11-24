package cc.sportsdb.common.config;

import cc.sportsdb.common.constant.RestConstant;
import cc.sportsdb.common.log.HttpClientInterceptor;
import cc.sportsdb.common.util.FrameworkUtil;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
@Import({Okhttp3Properties.class})
public class RestTemplateConfig {

    @Autowired
    private Okhttp3Properties okhttp3Properties;

    @Bean
    @Primary
    @LoadBalanced
    public RestTemplate restTemplate(OkHttp3ClientHttpRequestFactory factory) {
        return FrameworkUtil.enhanceRestTemplate(new RestTemplate(factory));
    }

    @Bean(name = RestConstant.RAW_NAME)
    public RestTemplate httpRestTemplate(OkHttp3ClientHttpRequestFactory factory) {
        return FrameworkUtil.enhanceRestTemplate(new RestTemplate(factory));
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
