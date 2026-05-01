package com.resale.platform.service.impl;

import com.resale.platform.common.BusinessException;
import com.resale.platform.common.ExceptionEnum;
import com.resale.platform.service.CaptchaService;
import com.wf.captcha.SpecCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 * 使用内存存储验证码（演示用途）
 *
 * @author MiniMax Agent
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    // 内存验证码缓存
    private final Map<String, CaptchaEntry> captchaCache = new ConcurrentHashMap<>();
    
    // 定时清理过期验证码
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * 验证码有效期（分钟）
     */
    @Value("${security.login.captcha-validity-minutes:5}")
    private Integer captchaValidityMinutes;

    /**
     * 验证码缓存Key前缀
     */
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";

    public CaptchaServiceImpl() {
        // 每分钟清理一次过期验证码
        scheduler.scheduleAtFixedRate(this::cleanExpiredCaptchas, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * 清理过期验证码
     */
    private void cleanExpiredCaptchas() {
        long now = System.currentTimeMillis();
        captchaCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    /**
     * 生成图形验证码
     *
     * @return 包含验证码图片Base64和Key的数组
     */
    @Override
    public Object[] generateCaptcha() {
        // 生成验证码
        SpecCaptcha captcha = new SpecCaptcha(120, 40, 5);
        captcha.setCharType(SpecCaptcha.TYPE_ONLY_NUMBER);
        String code = captcha.text().toLowerCase();
        
        // 生成唯一Key
        String key = UUID.randomUUID().toString().replace("-", "");
        
        // 存入内存缓存
        String cacheKey = CAPTCHA_KEY_PREFIX + key;
        long expireTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(captchaValidityMinutes);
        captchaCache.put(cacheKey, new CaptchaEntry(code, expireTime));
        
        // 生成图片Base64
        String base64Image = captcha.toBase64();
        
        log.debug("生成验证码，key={}, code={}", key, code);
        
        return new Object[]{base64Image, key};
    }

    /**
     * 验证图形验证码
     *
     * @param key 验证码Key
     * @param code 用户输入的验证码
     * @return 是否验证通过
     */
    @Override
    public boolean verifyCaptcha(String key, String code) {
        if (key == null || code == null) {
            throw new BusinessException(ExceptionEnum.CAPTCHA_REQUIRED);
        }
        
        String cacheKey = CAPTCHA_KEY_PREFIX + key;
        CaptchaEntry entry = captchaCache.get(cacheKey);
        
        if (entry == null || entry.isExpired(System.currentTimeMillis())) {
            captchaCache.remove(cacheKey);
            throw new BusinessException(ExceptionEnum.CAPTCHA_EXPIRED);
        }
        
        // 验证成功后删除验证码
        if (entry.getCode().equalsIgnoreCase(code)) {
            captchaCache.remove(cacheKey);
            return true;
        }
        
        return false;
    }

    /**
     * 删除验证码
     *
     * @param key 验证码Key
     */
    @Override
    public void deleteCaptcha(String key) {
        if (key != null) {
            String cacheKey = CAPTCHA_KEY_PREFIX + key;
            captchaCache.remove(cacheKey);
        }
    }

    /**
     * 验证码条目
     */
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
