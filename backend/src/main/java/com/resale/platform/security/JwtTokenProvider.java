package com.resale.platform.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT令牌提供者
 * 负责生成、解析和验证JWT令牌
 *
 * @author MiniMax Agent
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /**
     * JWT密钥
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * 访问令牌有效期（秒）
     */
    @Value("${jwt.access-token-validity-in-seconds}")
    private Long accessTokenValidityInSeconds;

    /**
     * 刷新令牌有效期（秒）
     */
    @Value("${jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenValidityInSeconds;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成访问令牌
     *
     * @param authentication 认证信息
     * @param rememberMe 是否记住我
     * @return JWT令牌
     */
    public String generateAccessToken(Authentication authentication, boolean rememberMe) {
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        return generateToken(userDetails.getUserId().toString(), rememberMe);
    }

    /**
     * 生成访问令牌
     *
     * @param userId 用户ID
     * @param rememberMe 是否记住我
     * @return JWT令牌
     */
    public String generateAccessToken(Long userId, boolean rememberMe) {
        return generateToken(userId.toString(), rememberMe);
    }

    /**
     * 生成令牌
     *
     * @param subject 主题（用户ID）
     * @param rememberMe 是否记住我
     * @return JWT令牌
     */
    private String generateToken(String subject, boolean rememberMe) {
        Date now = new Date();
        long validityInMillis = rememberMe ? refreshTokenValidityInSeconds * 1000 : accessTokenValidityInSeconds * 1000;
        Date validity = new Date(now.getTime() + validityInMillis);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .issuedAt(now)
                .expiration(validity)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * 获取令牌ID（jti）
     *
     * @param token JWT令牌
     * @return 令牌ID
     */
    public String getTokenId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getId();
    }

    /**
     * 获取令牌过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration();
    }

    /**
     * 验证令牌
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.error("无效的JWT签名");
        } catch (MalformedJwtException ex) {
            log.error("无效的JWT令牌");
        } catch (ExpiredJwtException ex) {
            log.error("已过期的JWT令牌");
        } catch (UnsupportedJwtException ex) {
            log.error("不支持的JWT令牌");
        } catch (IllegalArgumentException ex) {
            log.error("JWT Claims字符串为空");
        }
        return false;
    }

    /**
     * 获取令牌有效期（秒）
     *
     * @param rememberMe 是否记住我
     * @return 有效期（秒）
     */
    public Long getTokenValidityInSeconds(boolean rememberMe) {
        return rememberMe ? refreshTokenValidityInSeconds : accessTokenValidityInSeconds;
    }
}
