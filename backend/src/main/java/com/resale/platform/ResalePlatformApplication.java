package com.resale.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;

/**
 * C2C二手交易平台主启动类
 *
 * @author MiniMax Agent
 */
@SpringBootApplication(exclude = {
    RedisAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class
})
@MapperScan("com.resale.platform.mapper")
public class ResalePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResalePlatformApplication.class, args);
    }
}
