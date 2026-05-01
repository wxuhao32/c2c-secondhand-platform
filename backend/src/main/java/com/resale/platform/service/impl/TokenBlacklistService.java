package com.resale.platform.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TokenBlacklistService {

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private final Map<String, Long> tokenBlacklist = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    public TokenBlacklistService() {
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.MINUTES);
    }

    private void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        tokenBlacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }

    public void blacklistToken(String tokenId, long expirationTime) {
        tokenBlacklist.put(TOKEN_BLACKLIST_PREFIX + tokenId, expirationTime);
        log.info("Token已加入黑名单: tokenId={}", tokenId);
    }

    public boolean isBlacklisted(String tokenId) {
        Long expiration = tokenBlacklist.get(TOKEN_BLACKLIST_PREFIX + tokenId);
        if (expiration == null) {
            return false;
        }
        if (expiration < System.currentTimeMillis()) {
            tokenBlacklist.remove(TOKEN_BLACKLIST_PREFIX + tokenId);
            return false;
        }
        return true;
    }
}
