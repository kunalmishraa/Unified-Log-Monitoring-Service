package com.kunal.analytics.logmonitoring.repository;


import com.kunal.analytics.logmonitoring.entity.LogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends MongoRepository<LogEntry, String> {
}

