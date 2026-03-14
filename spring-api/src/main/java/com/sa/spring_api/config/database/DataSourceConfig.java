package com.sa.spring_api.config.database;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(
            @Qualifier("writeDataSource") DataSource write,
            @Qualifier("readDataSource") DataSource read) {

        RoutingDataSource routing = new RoutingDataSource();

        Map<Object, Object> targets = new HashMap<>();
        targets.put("WRITE", write);
        targets.put("READ", read);

        routing.setDefaultTargetDataSource(write);
        routing.setTargetDataSources(targets);
        routing.afterPropertiesSet();

        return routing;
    }
}
