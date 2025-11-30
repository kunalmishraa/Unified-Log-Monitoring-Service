package com.kunal.analytics.logmonitoring.controller;


import com.kunal.analytics.logmonitoring.dto.LogRequest;
import com.kunal.analytics.logmonitoring.dto.SearchRequest;
import com.kunal.analytics.logmonitoring.service.LogService;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestLog(@Valid @RequestBody LogRequest request) {
        logService.ingestLog(Map.of(
                "applicationName", request.getApplicationName(),
                "environment", request.getEnvironment(),
                "level", request.getLevel(),
                "message", request.getMessage(),
                "requestId", request.getRequestId(),
                "userId", request.getUserId(),
                "serviceName", request.getServiceName(),
                "host", request.getHost(),
                "duration", request.getDuration(),
                "timestamp", request.getTimestamp()
        ));
        return ResponseEntity.ok("Log ingested successfully");
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchLogs(@Valid @RequestBody SearchRequest request) {
        try {
            SearchResponse<Map> result = logService.searchLogs(request);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

