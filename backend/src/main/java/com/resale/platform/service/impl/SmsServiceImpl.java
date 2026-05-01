package com.resale.platform.service.impl;

import com.resale.platform.common.BusinessException;
import com.resale.platform.common.ExceptionEnum;
import com.resale.platform.dto.request.SmsLoginRequest;
import com.resale.platform.dto.response.LoginResponse;
import com.resale.platform.dto.response.UserInfoResponse;
import com.resale.platform.entity.User;
import com.resale.platform.mapper.UserMapper;
import com.resale.platform.security.JwtTokenProvider;
import com.resale.platform.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${security.login.sms-validity-minutes:5}")
    private Integer smsValidityMinutes;

    private static final String SMS_CODE_KEY_PREFIX = "sms:code:";
    private static final String SMS_SEND_INTERVAL_PREFIX = "sms:send:interval:";
    private static final int MAX_DAILY_SEND = 10;

    private final Map<String, SmsEntry> smsCodeCache = new ConcurrentHashMap<>();
    private final Map<String, Long> smsSendInterval = new ConcurrentHashMap<>();
    private final Map<String, Integer> dailySendCount = new ConcurrentHashMap<>();
    private final List<Map<String, Object>> recentSmsLog = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public SmsServiceImpl(UserMapper userMapper, JwtTokenProvider jwtTokenProvider,
                          AuthenticationManager authenticationManager) {
        this.userMapper = userMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;

        scheduler.scheduleAtFixedRate(this::cleanupExpired, 1, 1, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::resetDailyCount, 24, 24, TimeUnit.HOURS);
    }

    private void resetDailyCount() {
        dailySendCount.clear();
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        smsCodeCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        smsSendInterval.entrySet().removeIf(entry -> entry.getValue() < now);
    }

    @Override
    public String sendLoginCode(String mobile) {
        String intervalKey = SMS_SEND_INTERVAL_PREFIX + mobile;
        if (smsSendInterval.containsKey(intervalKey)) {
            throw new BusinessException(ExceptionEnum.SMS_SEND_TOO_FREQUENT);
        }

        String dailyKey = "daily:" + mobile;
        int count = dailySendCount.getOrDefault(dailyKey, 0);
        if (count >= MAX_DAILY_SEND) {
            throw new BusinessException(ExceptionEnum.SMS_SEND_TOO_FREQUENT);
        }
        dailySendCount.put(dailyKey, count + 1);

        String code = String.format("%06d", new Random().nextInt(1000000));
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String key = SMS_CODE_KEY_PREFIX + mobile + ":" + sessionId;

        long expireTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(smsValidityMinutes);
        smsCodeCache.put(key, new SmsEntry(code, mobile, expireTime));

        long intervalEnd = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60);
        smsSendInterval.put(intervalKey, intervalEnd);

        synchronized (recentSmsLog) {
            Map<String, Object> logEntry = new java.util.LinkedHashMap<>();
            logEntry.put("mobile", maskMobile(mobile));
            logEntry.put("code", code);
            logEntry.put("sendTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            logEntry.put("expireMinutes", smsValidityMinutes);
            recentSmsLog.add(logEntry);
            if (recentSmsLog.size() > 50) {
                recentSmsLog.remove(0);
            }
        }

        log.info("发送短信验证码: mobile={}, code={}", mobile, code);

        return key;
    }

    @Override
    public LoginResponse loginBySms(SmsLoginRequest request) {
        String mobile = request.getMobile();
        String smsCode = request.getSmsCode();

        if (!verifyCode(mobile, smsCode)) {
            throw new BusinessException(ExceptionEnum.SMS_CODE_INVALID);
        }

        boolean rememberMe = Boolean.TRUE.equals(request.getRememberMe());

        User user = userMapper.selectByMobile(mobile);
        boolean isNewUser = false;

        if (user == null) {
            user = createUserByMobile(mobile);
            isNewUser = true;
            log.info("短信登录创建新用户: mobile={}, userId={}", mobile, user.getUserId());
        }

        if (user.getStatus() != 1) {
            if (user.getStatus() == 2) {
                throw new BusinessException(ExceptionEnum.ACCOUNT_LOCKED);
            }
            throw new BusinessException(ExceptionEnum.ACCOUNT_DISABLED);
        }

        String token = jwtTokenProvider.generateAccessToken(user.getUserId(), rememberMe);
        Long expiresIn = jwtTokenProvider.getTokenValidityInSeconds(rememberMe);

        UserInfoResponse userInfo = buildUserInfoResponse(user);
        userInfo.setIsNewUser(isNewUser);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userInfo(userInfo)
                .isNewUser(isNewUser)
                .build();
    }

    @Override
    public boolean isMobileRegistered(String mobile) {
        return userMapper.selectByMobile(mobile) != null;
    }

    @Override
    public List<Map<String, Object>> getRecentSmsCodes() {
        synchronized (recentSmsLog) {
            return new ArrayList<>(recentSmsLog);
        }
    }

    @Override
    public boolean verifyCode(String mobile, String code) {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, SmsEntry> entry : smsCodeCache.entrySet()) {
            SmsEntry smsEntry = entry.getValue();
            if (smsEntry.getMobile().equals(mobile)
                    && smsEntry.getCode().equals(code)
                    && !smsEntry.isExpired(now)) {
                smsCodeCache.remove(entry.getKey());
                return true;
            }
        }
        return false;
    }

    private User createUserByMobile(String mobile) {
        User user = new User();
        user.setMobile(mobile);
        user.setUsername("user_" + mobile.substring(mobile.length() - 4));
        user.setStatus(1);
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    private UserInfoResponse buildUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .role(user.getRole())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .createTime(user.getCreatedAt())
                .build();
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) return "***";
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }

    private static class SmsEntry {
        private final String code;
        private final String mobile;
        private final long expireTime;

        public SmsEntry(String code, String mobile, long expireTime) {
            this.code = code;
            this.mobile = mobile;
            this.expireTime = expireTime;
        }

        public String getCode() { return code; }
        public String getMobile() { return mobile; }
        public boolean isExpired(long now) { return now > expireTime; }
    }
}
