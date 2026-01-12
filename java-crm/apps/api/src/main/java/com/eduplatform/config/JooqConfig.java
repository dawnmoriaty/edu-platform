package com.eduplatform.config;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * JooqConfig - Cấu hình jOOQ DSLContext và Configuration
 */
@org.springframework.context.annotation.Configuration
public class JooqConfig {

    @Bean
    @ConditionalOnMissingBean(Configuration.class)
    public Configuration jooqConfiguration(DataSource dataSource) {
        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.setDataSource(dataSource);
        configuration.setSQLDialect(SQLDialect.POSTGRES);
        
        Settings settings = new Settings()
                .withRenderSchema(false)
                .withExecuteLogging(true);
        configuration.setSettings(settings);
        
        return configuration;
    }

    @Bean
    @ConditionalOnMissingBean(DSLContext.class)
    public DSLContext dslContext(Configuration configuration) {
        return DSL.using(configuration);
    }
}
