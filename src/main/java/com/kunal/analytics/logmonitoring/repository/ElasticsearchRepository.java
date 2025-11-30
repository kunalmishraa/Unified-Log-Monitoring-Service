package com.kunal.analytics.logmonitoring.repository;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.util.Map;

@Repository
public class ElasticsearchRepository {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public void indexLog(String indexName, Map<String, Object> logData) throws IOException {
        BulkRequest.Builder br = new BulkRequest.Builder();
        br.operations(op -> op
                .index(idx -> idx
                        .index(indexName)
                        .document(logData)
                )
        );
        elasticsearchClient.bulk(br.build());
    }

    public SearchResponse<Map> searchLogs(SearchRequest searchRequest) throws IOException {
        return elasticsearchClient.search(searchRequest, Map.class);
    }

    public String getDailyIndexName() {
        return "logs-" + java.time.LocalDate.now().toString();
    }
}

