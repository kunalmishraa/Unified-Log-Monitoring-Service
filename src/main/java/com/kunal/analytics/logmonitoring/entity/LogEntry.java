package com.kunal.analytics.logmonitoring.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "log_metadata")
public class LogEntry {
    @Id
    private String id;
    private String logId;
    private String applicationName;
    private String environment;
    private String level;
    private String message;
    private Instant timestamp;
    private String requestId;
    private String userId;
    private String serviceName;
    private String host;
    private long duration;
}

