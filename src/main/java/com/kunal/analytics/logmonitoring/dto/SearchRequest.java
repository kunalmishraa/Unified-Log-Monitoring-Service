package com.kunal.analytics.logmonitoring.dto;


import lombok.Data;

import java.time.Instant;

@Data
public class SearchRequest {
    private String query = "*";
    private String applicationName;
    private String level;
    private Instant from;
    private Instant to;
    private int size = 50;
    private int fromIndex = 0;
}

