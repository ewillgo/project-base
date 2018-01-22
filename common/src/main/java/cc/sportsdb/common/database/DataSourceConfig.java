package cc.sportsdb.common.database;

import cc.sportsdb.common.util.ToolUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.boot.bind.PropertySourcesBinder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.BindException;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig implements EnvironmentAware {

    private Environment environment;

    @Bean
    public AbstractRoutingDataSource dataSource(DataSourceList dataSourceList) throws BindException {
        PropertySourcesBinder builder = new PropertySourcesBinder((ConfigurableEnvironment) environment);

        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();

        for (String dataSourceName : dataSourceList.getDataSourceNames()) {
            HikariConfig config = new HikariConfig();
            PropertiesConfigurationFactory<HikariConfig> factory = new PropertiesConfigurationFactory<>(config);
            factory.setPropertySources(builder.getPropertySources());
            factory.setTargetName(dataSourceName);
            factory.bindPropertiesToTarget();

            config.setJdbcUrl(ToolUtil.decodeUrl(config.getJdbcUrl()));
            config.setPassword(ToolUtil.decodeUrl(config.getPassword()));
            HikariDataSource ds = new HikariDataSource(config);
            if (dataSourceName.equals(dataSourceList.getDefaultDataSourceName())) {
                dynamicDataSource.setDefaultTargetDataSource(ds);
            }

            dataSourceMap.put(dataSourceName, ds);
        }

        dynamicDataSource.setTargetDataSources(dataSourceMap);
        return dynamicDataSource;
    }

    @ConditionalOnMissingBean
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(AbstractRoutingDataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public DataSourceSwitchAspect dataSourceSwitchAspect() {
        return new DataSourceSwitchAspect();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    static class DynamicDataSource extends AbstractRoutingDataSource {
        @Override
        protected Object determineCurrentLookupKey() {
            return DynamicDataSourceHolder.peekCurrentDataSourceName();
        }
    }
}
