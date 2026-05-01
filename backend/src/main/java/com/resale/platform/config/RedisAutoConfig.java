package com.resale.platform.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true")
@Import({RedisConfig.class})
public class RedisAutoConfig {
}
