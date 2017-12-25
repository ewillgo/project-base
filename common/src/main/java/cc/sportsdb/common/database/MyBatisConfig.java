package cc.sportsdb.common.database;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisConfig {

    @Bean
    public MyBatisInterceptor myBatisInterceptor() {
        MyBatisInterceptor myBatisInterceptor = new MyBatisInterceptor();
        return myBatisInterceptor;
    }

}
