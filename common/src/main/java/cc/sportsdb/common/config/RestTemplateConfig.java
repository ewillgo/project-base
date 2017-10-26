package cc.sportsdb.common.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
@Import({Okhttp3Properties.class})
public class RestTemplateConfig {

    @Autowired
    private Okhttp3Properties okhttp3Properties;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(okHttp3ClientHttpRequestFactory());
    }

    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .readTimeout(okhttp3Properties.getReadTimeout(), TimeUnit.SECONDS)
                .connectTimeout(okhttp3Properties.getConnectTimeout(), TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(okhttp3Properties.getMaxIdleConnections(), okhttp3Properties.getKeepAliveDuration(), TimeUnit.MINUTES))
                .build();
    }

    public OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory() {
        return new OkHttp3ClientHttpRequestFactory(okHttpClient());
    }
}
