package com.resale.platform.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

public class RedisConditionProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String redisEnabled = environment.getProperty("spring.data.redis.enabled", "false");
        Properties properties = new Properties();
        if ("false".equals(redisEnabled)) {
            properties.setProperty("spring.autoconfigure.exclude",
                    "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                    "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration");
        }
        environment.getPropertySources().addFirst(new PropertiesPropertySource("redisConditionConfig", properties));
    }
}
