package com.kunal.analytics.logmonitoring.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document(collection = "dashboards")
public class Dashboard {
    @Id
    private String id;
    private String name;
    private String userId;
    private Map<String, Object> kibanaConfig;
    private String elasticsearchIndex;
}
