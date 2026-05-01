package com.resale.platform.config;

import com.resale.platform.service.CacheService;
import com.resale.platform.service.impl.LocalCacheServiceImpl;
import com.resale.platform.service.impl.RedisCacheServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@Configuration
public class CacheStrategyConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true")
    public CacheService redisCacheService(RedisTemplate<String, Object> redisTemplate,
                                          StringRedisTemplate stringRedisTemplate) {
        log.info("Cache strategy: Redis");
        return new RedisCacheServiceImpl(redisTemplate, stringRedisTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "false", matchIfMissing = true)
    public CacheService localCacheService() {
        log.info("Cache strategy: Local (Caffeine-like in-memory)");
        return new LocalCacheServiceImpl();
    }
}
