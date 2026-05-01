package com.resale.platform.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT令牌提供者单元测试")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "ResalePlatformSecretKey2024ForJWTSigningMustBeLongEnough12345678901234567890");
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenValidityInSeconds", 7200L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenValidityInSeconds", 604800L);
    }

    @Test
    @DisplayName("生成访问令牌")
    void generateAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(1L, false);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("从令牌获取用户ID")
    void getUserIdFromToken() {
        String token = jwtTokenProvider.generateAccessToken(123L, false);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(123L, userId);
    }

    @Test
    @DisplayName("验证有效令牌")
    void validateToken_valid() {
        String token = jwtTokenProvider.generateAccessToken(1L, false);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("验证无效令牌")
    void validateToken_invalid() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    @DisplayName("验证空令牌")
    void validateToken_empty() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    @DisplayName("记住我模式生成更长的有效期")
    void rememberMeToken() {
        Long normalValidity = jwtTokenProvider.getTokenValidityInSeconds(false);
        Long rememberValidity = jwtTokenProvider.getTokenValidityInSeconds(true);
        assertTrue(rememberValidity > normalValidity);
    }
}
