package com.resale.platform.service.impl;

import com.resale.platform.common.BusinessException;
import com.resale.platform.common.ExceptionEnum;
import com.resale.platform.service.CaptchaService;
import com.wf.captcha.SpecCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private final Map<String, CaptchaEntry> captchaCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Value("${security.login.captcha-validity-minutes:5}")
    private Integer captchaValidityMinutes;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final String BASE64_PREFIX = "data:image/png;base64,";
    private static final int CAPTCHA_WIDTH = 120;
    private static final int CAPTCHA_HEIGHT = 40;
    private static final int CAPTCHA_LENGTH = 5;

    @PostConstruct
    public void init() {
        System.setProperty("java.awt.headless", "true");
        log.info("验证码服务初始化完成，headless模式已启用");
        
        scheduler.scheduleAtFixedRate(this::cleanExpiredCaptchas, 1, 1, TimeUnit.MINUTES);
        
        try {
            SpecCaptcha warmup = new SpecCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, CAPTCHA_LENGTH);
            warmup.setCharType(SpecCaptcha.TYPE_ONLY_NUMBER);
            warmup.text();
            log.info("验证码生成器预热完成");
        } catch (Exception e) {
            log.warn("验证码生成器预热失败: {}", e.getMessage());
        }
    }

    private void cleanExpiredCaptchas() {
        long now = System.currentTimeMillis();
        int removedCount = 0;
        for (Map.Entry<String, CaptchaEntry> entry : captchaCache.entrySet()) {
            if (entry.getValue().isExpired(now)) {
                captchaCache.remove(entry.getKey());
                removedCount++;
            }
        }
        if (removedCount > 0) {
            log.debug("清理过期验证码: {} 个", removedCount);
        }
    }

    @Override
    public Object[] generateCaptcha() {
        long startTime = System.currentTimeMillis();
        String key = null;
        String code = null;
        
        try {
            key = UUID.randomUUID().toString().replace("-", "");
            
            SpecCaptcha captcha = new SpecCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, CAPTCHA_LENGTH);
            captcha.setCharType(SpecCaptcha.TYPE_ONLY_NUMBER);
            
            code = captcha.text().toLowerCase();
            
            String cacheKey = CAPTCHA_KEY_PREFIX + key;
            long expireTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(captchaValidityMinutes);
            captchaCache.put(cacheKey, new CaptchaEntry(code, expireTime));
            
            String base64Image = captcha.toBase64();
            
            if (base64Image == null || base64Image.isEmpty()) {
                throw new RuntimeException("验证码图片生成失败: Base64为空");
            }
            
            String fullBase64 = base64Image.startsWith("data:") ? base64Image : BASE64_PREFIX + base64Image;
            
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("验证码生成成功: key={}, 耗时={}ms", key, elapsed);
            
            if (elapsed > 1000) {
                log.warn("验证码生成耗时过长: {}ms，建议检查服务器性能", elapsed);
            }
            
            return new Object[]{fullBase64, key};
            
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("验证码生成失败: key={}, code={}, 耗时={}ms, 错误={}", key, code, elapsed, e.getMessage(), e);
            throw new BusinessException(ExceptionEnum.INTERNAL_ERROR.getCode(), "验证码生成失败，请刷新重试");
        }
    }

    @Override
    public boolean verifyCaptcha(String key, String code) {
        if (key == null || key.trim().isEmpty()) {
            log.warn("验证码校验失败: key为空");
            throw new BusinessException(ExceptionEnum.CAPTCHA_REQUIRED);
        }
        
        if (code == null || code.trim().isEmpty()) {
            log.warn("验证码校验失败: code为空, key={}", key);
            throw new BusinessException(ExceptionEnum.CAPTCHA_REQUIRED);
        }
        
        String cacheKey = CAPTCHA_KEY_PREFIX + key;
        CaptchaEntry entry = captchaCache.get(cacheKey);
        
        if (entry == null) {
            log.warn("验证码校验失败: 验证码不存在, key={}", key);
            throw new BusinessException(ExceptionEnum.CAPTCHA_EXPIRED);
        }
        
        if (entry.isExpired(System.currentTimeMillis())) {
            captchaCache.remove(cacheKey);
            log.warn("验证码校验失败: 验证码已过期, key={}", key);
            throw new BusinessException(ExceptionEnum.CAPTCHA_EXPIRED);
        }
        
        if (entry.getCode().equalsIgnoreCase(code.trim())) {
            captchaCache.remove(cacheKey);
            log.debug("验证码校验成功: key={}", key);
            return true;
        }
        
        log.warn("验证码校验失败: 验证码错误, key={}, input={}, expected={}", key, code, entry.getCode());
        throw new BusinessException(ExceptionEnum.CAPTCHA_ERROR);
    }

    @Override
    public void deleteCaptcha(String key) {
        if (key != null) {
            String cacheKey = CAPTCHA_KEY_PREFIX + key;
            captchaCache.remove(cacheKey);
        }
    }

    private static class CaptchaEntry {
        private final String code;
        private final long expireTime;

        public CaptchaEntry(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }

        public String getCode() {
            return code;
        }

        public boolean isExpired(long now) {
            return now > expireTime;
        }
    }
}
