package com.resale.platform.config;

import com.resale.platform.entity.User;
import com.resale.platform.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    public void run(String... args) {
        ensureTestUsersHaveKnownPasswords();
    }

    private void ensureTestUsersHaveKnownPasswords() {
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        updateUserPassword(1L, "admin", encodedPassword);
        updateUserPassword(2L, "user1", encodedPassword);
        updateUserPassword(3L, "user2", encodedPassword);

        log.info("测试用户密码已初始化为: {}", DEFAULT_PASSWORD);
    }

    private void updateUserPassword(Long userId, String username, String encodedPassword) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(encodedPassword);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
            log.info("用户 {} (ID={}) 密码已重置", username, userId);
        } else {
            log.warn("用户 {} (ID={}) 不存在，跳过密码重置", username, userId);
        }
    }
}
