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
    }

    private void ensureTestUsersHaveKnownPasswords() {
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        jdbcTemplate.update("UPDATE users SET password = ? WHERE username = 'admin'", encodedPassword);
        jdbcTemplate.update("UPDATE users SET password = ? WHERE username = 'user1'", encodedPassword);
        jdbcTemplate.update("UPDATE users SET password = ? WHERE username = 'user2'", encodedPassword);

        log.info("测试用户密码已初始化为: {}", DEFAULT_PASSWORD);
    }
}
