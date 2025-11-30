package com.kunal.analytics.logmonitoring.service;


import com.kunal.analytics.logmonitoring.cache.LogCacheService;
import com.kunal.analytics.logmonitoring.dto.SearchRequest;
import com.kunal.analytics.logmonitoring.entity.LogEntry;
import com.kunal.analytics.logmonitoring.repository.ElasticsearchRepository;
import com.kunal.analytics.logmonitoring.repository.LogRepository;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Map;

@Service
public class LogService {

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private ElasticsearchRepository elasticsearchRepository;

    @Autowired
    private LogCacheService cacheService;

    public void ingestLog(Map<String, Object> logData) {
        try {
            String indexName = elasticsearchRepository.getDailyIndexName();
            elasticsearchRepository.indexLog(indexName, logData);

            // Cache error patterns
            if ("ERROR".equals(logData.get("level"))) {
                cacheService.incrementErrorCount(
                        java.time.LocalDate.now().toString(),
                        (String) logData.get("message")
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to index log", e);
        }
    }

    public SearchResponse<Map> searchLogs(SearchRequest request) throws IOException {
        String cacheKey = "search:" + request.getQuery() + ":" + request.getFromIndex();
        Object cached = cacheService.getCachedSearch(cacheKey);

        if (cached != null) {
            // Return cached result (simplified)
            return (SearchResponse<Map>) cached;
        }

        // Build Elasticsearch search request
        var searchReq = SearchRequest.of(s -> s
                .index("logs-*")
                .query(q -> q
                        .multiMatch(t -> t
                                .fields("message", "serviceName", "applicationName")
                                .query(request.getQuery())
                        )
                )
                .size(request.getSize())
                .from(request.getFromIndex())
        );

        SearchResponse<Map> result = elasticsearchRepository.searchLogs(searchReq);
        cacheService.cacheSearchResult(cacheKey, result, 300); // 5min cache
        return result;
    }
}
