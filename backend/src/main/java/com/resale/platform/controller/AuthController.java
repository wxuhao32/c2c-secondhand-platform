package com.resale.platform.controller;

import com.resale.platform.common.BusinessException;
import com.resale.platform.common.ExceptionEnum;
import com.resale.platform.common.Result;
import com.resale.platform.dto.request.LoginRequest;
import com.resale.platform.dto.request.RegisterRequest;
import com.resale.platform.dto.request.SendSmsRequest;
import com.resale.platform.dto.request.SmsLoginRequest;
import com.resale.platform.dto.response.LoginResponse;
import com.resale.platform.dto.response.UserInfoResponse;
import com.resale.platform.security.JwtTokenProvider;
import com.resale.platform.security.SecurityUser;
import com.resale.platform.service.AuthService;
import com.resale.platform.service.CaptchaService;
import com.resale.platform.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证授权", description = "用户登录、注册、短信验证、登出等认证接口")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;
    private final SmsService smsService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码", description = "生成图形验证码，返回Base64图片和验证Key")
    public Result<Map<String, String>> getCaptcha() {
        Object[] result = captchaService.generateCaptcha();
        Map<String, String> data = new HashMap<>();
        data.put("image", (String) result[0]);
        data.put("key", (String) result[1]);
        return Result.success(data);
    }

    @PostMapping("/login")
    @Operation(summary = "账号密码登录", description = "使用用户名/手机号+密码登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        log.info("登录请求: account={}, ip={}", maskAccount(request.getAccount()), ipAddress);
        
        LoginResponse response = authService.login(request, ipAddress);
        log.info("登录成功: userId={}", response.getUserInfo().getUserId());
        
        return Result.success(response);
    }

    @PostMapping("/sendSms")
    @Operation(summary = "发送短信验证码", description = "发送手机短信验证码，需先通过图形验证码校验")
    public Result<Map<String, String>> sendSms(@Valid @RequestBody SendSmsRequest request) {
        // 校验图形验证码
        boolean captchaValid = captchaService.verifyCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
        if (!captchaValid) {
            throw new BusinessException(ExceptionEnum.CAPTCHA_ERROR);
        }
        
        // 发送短信
        String smsKey = smsService.sendLoginCode(request.getMobile());
        
        Map<String, String> data = new HashMap<>();
        data.put("smsKey", smsKey);
        
        return Result.success("验证码发送成功", data);
    }

    @PostMapping("/loginBySms")
    @Operation(summary = "短信验证码登录", description = "使用手机号+短信验证码登录，新用户自动注册")
    public Result<LoginResponse> loginBySms(@Valid @RequestBody SmsLoginRequest request) {
        log.info("短信登录请求: mobile={}", maskMobile(request.getMobile()));
        
        LoginResponse response = smsService.loginBySms(request);
        
        // 根据是否是新用户返回不同状态码
        if (Boolean.TRUE.equals(response.getIsNewUser())) {
            return Result.created(response);
        }
        
        return Result.success(response);
    }

    @GetMapping("/wechat/callback")
    @Operation(summary = "微信OAuth回调", description = "微信授权登录回调接口")
    public Result<Map<String, String>> wechatCallback(@RequestParam String code, @RequestParam String state) {
        log.info("微信回调: code={}, state={}", code, state);
        
        LoginResponse response = authService.wechatLogin(code, state);
        
        Map<String, String> data = new HashMap<>();
        data.put("token", response.getToken());
        data.put("userId", response.getUserInfo().getUserId().toString());
        data.put("isNewUser", response.getIsNewUser().toString());
        
        return Result.success(data);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户名+密码注册新账号")
    public Result<Map<String, Long>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("注册请求: username={}, mobile={}", request.getUsername(), maskMobile(request.getMobile()));
        
        Long userId = authService.register(request);
        
        Map<String, Long> data = new HashMap<>();
        data.put("userId", userId);
        
        log.info("注册成功: userId={}", userId);
        return Result.created(data);
    }

    @PostMapping("/logout")
    @Operation(summary = "登出", description = "用户登出，将Token加入黑名单")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            authService.logout(token);
            log.info("登出成功");
        }
        return Result.success("登出成功", null);
    }

    @GetMapping("/currentUser")
    @Operation(summary = "获取当前用户", description = "获取当前登录用户的详细信息")
    public Result<UserInfoResponse> getCurrentUser() {
        SecurityUser userDetails = getCurrentUserDetails();
        UserInfoResponse userInfo = authService.getCurrentUser(userDetails.getUserId());
        return Result.success(userInfo);
    }

    @GetMapping("/sms-codes")
    @Operation(summary = "获取最近验证码", description = "开发环境获取最近发送的短信验证码列表")
    public Result<java.util.List<Map<String, Object>>> getRecentSmsCodes() {
        return Result.success(smsService.getRecentSmsCodes());
    }

    /**
     * 获取当前登录用户详情
     */
    private SecurityUser getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (SecurityUser) authentication.getPrincipal();
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 掩码账号（脱敏处理）
     */
    private String maskAccount(String account) {
        if (account == null || account.length() < 3) {
            return "***";
        }
        if (account.contains("@")) {
            // 邮箱：保留前缀前两位和域名
            int atIndex = account.indexOf("@");
            if (atIndex > 2) {
                return account.substring(0, 2) + "***" + account.substring(atIndex);
            }
        }
        // 手机号或用户名：保留首尾
        return account.substring(0, 2) + "***" + account.substring(account.length() - 1);
    }

    /**
     * 掩码手机号（脱敏处理）
     */
    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return "***";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
