package com.resale.platform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseStartupChecker {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void checkDatabase() {
        try {
            Connection conn = dataSource.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            String dbName = metaData.getDatabaseProductName();
            String dbVersion = metaData.getDatabaseProductVersion();
            String dbUrl = metaData.getURL();
            conn.close();

            log.info("========================================");
            log.info("数据库连接成功!");
            log.info("数据库类型: {} {}", dbName, dbVersion);
            log.info("数据库URL: {}", dbUrl);
            log.info("========================================");

            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            log.info("用户表记录数: {}", userCount);

            if (userCount != null && userCount > 0) {
                log.info("数据库初始化正常，已有 {} 个用户", userCount);
            } else {
                log.warn("用户表为空，请检查数据初始化脚本是否执行成功");
            }

        } catch (Exception e) {
            log.error("========================================");
            log.error("数据库连接/检查失败: {}", e.getMessage());
            log.error("请检查数据库配置是否正确");
            log.error("========================================");
        }
    }
}
