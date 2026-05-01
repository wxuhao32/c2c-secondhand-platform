package com.resale.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.resale.platform.mapper")
@EnableAsync
public class ResalePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResalePlatformApplication.class, args);
    }
}
