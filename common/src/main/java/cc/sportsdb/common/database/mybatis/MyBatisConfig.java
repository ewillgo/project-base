package cc.sportsdb.common.database.mybatis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisConfig {

    @Bean
    public MyBatisInterceptor myBatisInterceptor() {
        return new MyBatisInterceptor();
    }

}
