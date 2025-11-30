package com.kunal.analytics.logmonitoring.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${elasticsearch.url:localhost:9200}")
    private String elasticsearchUrl;

    @Value("${spring.application.name:unified-log-platform}")
    private String appName;

    // Getters...
    public String getElasticsearchUrl() { return elasticsearchUrl; }
    public String getAppName() { return appName; }
}
