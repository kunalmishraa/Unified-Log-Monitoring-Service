package com.kunal.analytics.logmonitoring.cache;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class LogCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void cacheSearchResult(String cacheKey, Object result, long ttlSeconds) {
        redisTemplate.opsForValue().set(cacheKey, result, ttlSeconds, TimeUnit.SECONDS);
    }

    public Object getCachedSearch(String cacheKey) {
        return redisTemplate.opsForValue().get(cacheKey);
    }

    public void incrementErrorCount(String dateKey, String errorMessage) {
        redisTemplate.opsForZSet().incrementScore("top_errors:" + dateKey, errorMessage, 1);
    }

    public Set<Object> getTopErrors(String dateKey, long count) {
        return redisTemplate.opsForZSet().reverseRange("top_errors:" + dateKey, 0, count - 1);
    }

    public void cacheDashboard(String userId, String dashboardId, Object config) {
        redisTemplate.opsForHash().put("user_dashboards:" + userId, dashboardId, config);
    }
}
