package com.resale.platform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    public void run(String... args) {
        ensureTestUsersHaveKnownPasswords();
        resetAllSequences();
    }

    private void ensureTestUsersHaveKnownPasswords() {
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        jdbcTemplate.update("UPDATE users SET password = ? WHERE id = 1", encodedPassword);
        jdbcTemplate.update("UPDATE users SET password = ? WHERE id = 2", encodedPassword);
        jdbcTemplate.update("UPDATE users SET password = ? WHERE id = 3", encodedPassword);

        log.info("测试用户密码已初始化为: {}", DEFAULT_PASSWORD);
    }

    private void resetAllSequences() {
        resetSequence("users");
        resetSequence("category");
        resetSequence("goods");
        resetSequence("orders");
        resetSequence("favorite");
        resetSequence("message");
        resetSequence("audit_log");
        resetSequence("user_auth");
        resetSequence("login_fail_record");
        resetSequence("chat_conversation");
        resetSequence("chat_message");
        resetSequence("comment");
        resetSequence("user_address");
    }

    private void resetSequence(String tableName) {
        try {
            Integer maxId = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(id), 0) FROM " + tableName, Integer.class);
            if (maxId != null) {
                jdbcTemplate.execute("ALTER TABLE " + tableName
                        + " AUTO_INCREMENT = " + (maxId + 1));
                log.info("表 {} 序列已重置，下一个ID: {}", tableName, maxId + 1);
            }
        } catch (Exception e) {
            log.warn("重置表 {} 序列失败: {}", tableName, e.getMessage());
        }
    }
}
