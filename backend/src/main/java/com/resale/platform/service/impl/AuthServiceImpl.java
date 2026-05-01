package com.resale.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resale.platform.common.BusinessException;
import com.resale.platform.common.ExceptionEnum;
import com.resale.platform.dto.request.LoginRequest;
import com.resale.platform.dto.request.RegisterRequest;
import com.resale.platform.dto.response.LoginResponse;
import com.resale.platform.dto.response.UserInfoResponse;
import com.resale.platform.entity.User;
import com.resale.platform.mapper.UserMapper;
import com.resale.platform.security.JwtTokenProvider;
import com.resale.platform.security.SecurityUser;
import com.resale.platform.service.AuthService;
import com.resale.platform.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CaptchaService captchaService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${security.login.max-fail-count:5}")
    private Integer maxFailCount;

    @Value("${security.login.lock-duration-minutes:15}")
    private Integer lockDurationMinutes;

    @Override
    public LoginResponse login(LoginRequest request, String ipAddress) {
        String account = request.getAccount();
        String password = request.getPassword();
        boolean rememberMe = Boolean.TRUE.equals(request.getRememberMe());

        boolean captchaValid = captchaService.verifyCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
        if (!captchaValid) {
            throw new BusinessException(ExceptionEnum.CAPTCHA_ERROR);
        }

        User user = findUserByAccount(account);
        if (user == null) {
            throw new BusinessException(ExceptionEnum.ACCOUNT_NOT_FOUND);
        }

        checkUserStatus(user);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            handleLoginFailure(user, account);
            throw new BusinessException(ExceptionEnum.PASSWORD_ERROR);
        }

        handleLoginSuccess(user, ipAddress);

        String token = jwtTokenProvider.generateAccessToken(user.getUserId(), rememberMe);
        Long expiresIn = jwtTokenProvider.getTokenValidityInSeconds(rememberMe);

        UserInfoResponse userInfo = buildUserInfoResponse(user);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userInfo(userInfo)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request) {
        boolean captchaValid = captchaService.verifyCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
        if (!captchaValid) {
            throw new BusinessException(ExceptionEnum.CAPTCHA_ERROR);
        }

        User existUser = userMapper.selectByUsername(request.getUsername());
        if (existUser != null) {
            throw new BusinessException(ExceptionEnum.USERNAME_EXIST);
        }

        existUser = userMapper.selectByMobile(request.getMobile());
        if (existUser != null) {
            throw new BusinessException(ExceptionEnum.MOBILE_EXIST);
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            existUser = userMapper.selectByEmail(request.getEmail());
            if (existUser != null) {
                throw new BusinessException(ExceptionEnum.EMAIL_EXIST);
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setMobile(request.getMobile());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功: userId={}, username={}", user.getUserId(), user.getUsername());

        return user.getUserId();
    }

    @Override
    public void logout(String token) {
        try {
            String tokenId = jwtTokenProvider.getTokenId(token);
            Long expiration = jwtTokenProvider.getExpirationFromToken(token).getTime();
            long ttl = expiration - System.currentTimeMillis();

            if (ttl > 0) {
                tokenBlacklistService.blacklistToken(tokenId, expiration);
            }
        } catch (Exception e) {
            log.error("登出处理失败", e);
        }
    }

    @Override
    public LoginResponse wechatLogin(String code, String state) {
        log.info("微信登录: code={}, state={}", code, state);
        String openid = "mock_openid_" + code;

        User user = userMapper.selectByWechatOpenid(openid);
        boolean isNewUser = false;

        if (user == null) {
            user = new User();
            user.setUsername("wechat_" + System.currentTimeMillis());
            user.setStatus(1);
            user.setRole("USER");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
            isNewUser = true;
            log.info("微信登录创建新用户: userId={}, openid={}", user.getUserId(), openid);
        }

        String token = jwtTokenProvider.generateAccessToken(user.getUserId(), false);
        Long expiresIn = jwtTokenProvider.getTokenValidityInSeconds(false);

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
    public UserInfoResponse getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ExceptionEnum.USER_NOT_FOUND);
        }
        return buildUserInfoResponse(user);
    }

    private User findUserByAccount(String account) {
        User user = userMapper.selectByUsername(account);
        if (user != null) return user;
        user = userMapper.selectByMobile(account);
        if (user != null) return user;
        return userMapper.selectByEmail(account);
    }

    private void checkUserStatus(User user) {
        if (user.getStatus() == 0) {
            throw new BusinessException(ExceptionEnum.ACCOUNT_DISABLED);
        }
    }

    private void handleLoginFailure(User user, String account) {
        log.warn("用户登录失败: userId={}, account={}", user.getUserId(), account);
    }

    private void handleLoginSuccess(User user, String ipAddress) {
        user.setLastLoginIp(ipAddress);
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("用户登录成功: userId={}, ip={}", user.getUserId(), ipAddress);
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
}
