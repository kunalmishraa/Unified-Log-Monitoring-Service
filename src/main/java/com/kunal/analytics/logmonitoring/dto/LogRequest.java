package com.kunal.analytics.logmonitoring.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class LogRequest {
    @NotBlank
    private String applicationName;
    private String environment = "production";
    @NotBlank
    private String level;
    @NotBlank
    private String message;
    private String requestId;
    private String userId;
    private String serviceName;
    private String host;
    private long duration;
    private Instant timestamp = Instant.now();
}
