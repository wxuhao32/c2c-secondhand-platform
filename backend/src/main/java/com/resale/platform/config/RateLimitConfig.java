package com.resale.platform.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@ConfigurationProperties(prefix = "rate-limit")
@Data
public class RateLimitConfig {

    private boolean enabled = true;
    private int defaultCapacity = 100;
    private int defaultRefillTokens = 100;
    private int defaultRefillDuration = 60;
    private Map<String, ApiLimit> apiLimits = new ConcurrentHashMap<>();

    @Data
    public static class ApiLimit {
        private int capacity;
        private int refillTokens;
        private int refillDuration;
    }

    @Bean
    public ConcurrentHashMap<String, Bucket> rateLimitBuckets() {
        return new ConcurrentHashMap<>();
    }

    public Bucket resolveBucket(String key) {
        ConcurrentHashMap<String, Bucket> buckets = rateLimitBuckets();
        return buckets.computeIfAbsent(key, k -> createBucket(k));
    }

    private Bucket createBucket(String apiGroup) {
        ApiLimit limit = apiLimits.get(apiGroup);
        if (limit != null) {
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(
                            limit.getCapacity(),
                            Refill.intervally(limit.getRefillTokens(), Duration.ofSeconds(limit.getRefillDuration()))
                    ))
                    .build();
        }
        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                        defaultCapacity,
                        Refill.intervally(defaultRefillTokens, Duration.ofSeconds(defaultRefillDuration))
                ))
                .build();
    }
}
