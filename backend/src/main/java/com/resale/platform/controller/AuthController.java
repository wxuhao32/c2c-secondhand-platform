package com.resale.platform.controller;

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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、注册、登出等认证相关请求
 *
 * @author MiniMax Agent
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;
    private final SmsService smsService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 获取图形验证码
     * GET /api/auth/captcha
     *
     * @return 包含验证码图片Base64和Key
     */
    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        Object[] result = captchaService.generateCaptcha();
        Map<String, String> data = new HashMap<>();
        data.put("image", (String) result[0]);
        data.put("key", (String) result[1]);
        return Result.success(data);
    }

    /**
     * 账号密码登录
     * POST /api/auth/login
     *
     * @param request 登录请求
     * @param httpRequest HTTP请求（用于获取IP）
     * @return 登录响应
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        log.info("登录请求: account={}, ip={}", maskAccount(request.getAccount()), ipAddress);
        
        LoginResponse response = authService.login(request, ipAddress);
        log.info("登录成功: userId={}", response.getUserInfo().getUserId());
        
        return Result.success(response);
    }

    /**
     * 发送短信验证码
     * POST /api/auth/sendSms
     *
     * @param request 发送短信请求
     * @return 验证码Key
     */
    @PostMapping("/sendSms")
    public Result<Map<String, String>> sendSms(@Valid @RequestBody SendSmsRequest request) {
        // 校验图形验证码
        captchaService.verifyCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
        
        // 发送短信
        String smsKey = smsService.sendLoginCode(request.getMobile());
        
        Map<String, String> data = new HashMap<>();
        data.put("smsKey", smsKey);
        
        return Result.success("验证码发送成功", data);
    }

    /**
     * 手机验证码登录
     * POST /api/auth/loginBySms
     *
     * @param request 短信登录请求
     * @return 登录响应
     */
    @PostMapping("/loginBySms")
    public Result<LoginResponse> loginBySms(@Valid @RequestBody SmsLoginRequest request) {
        log.info("短信登录请求: mobile={}", maskMobile(request.getMobile()));
        
        LoginResponse response = smsService.loginBySms(request);
        
        // 根据是否是新用户返回不同状态码
        if (Boolean.TRUE.equals(response.getIsNewUser())) {
            return Result.created(response);
        }
        
        return Result.success(response);
    }

    /**
     * 微信OAuth回调
     * GET /api/auth/wechat/callback
     *
     * @param code 微信授权码
     * @param state 状态参数
     * @return 重定向到前端
     */
    @GetMapping("/wechat/callback")
    public Result<Map<String, String>> wechatCallback(@RequestParam String code, @RequestParam String state) {
        log.info("微信回调: code={}, state={}", code, state);
        
        LoginResponse response = authService.wechatLogin(code, state);
        
        Map<String, String> data = new HashMap<>();
        data.put("token", response.getToken());
        data.put("userId", response.getUserInfo().getUserId().toString());
        data.put("isNewUser", response.getIsNewUser().toString());
        
        return Result.success(data);
    }

    /**
     * 用户注册
     * POST /api/auth/register
     *
     * @param request 注册请求
     * @return 用户ID
     */
    @PostMapping("/register")
    public Result<Map<String, Long>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("注册请求: username={}, mobile={}", request.getUsername(), maskMobile(request.getMobile()));
        
        Long userId = authService.register(request);
        
        Map<String, Long> data = new HashMap<>();
        data.put("userId", userId);
        
        log.info("注册成功: userId={}", userId);
        return Result.created(data);
    }

    /**
     * 登出
     * POST /api/auth/logout
     *
     * @param authorization Authorization头
     * @return 结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            authService.logout(token);
            log.info("登出成功");
        }
        return Result.success("登出成功", null);
    }

    /**
     * 获取当前用户信息
     * GET /api/auth/currentUser
     *
     * @return 用户信息
     */
    @GetMapping("/currentUser")
    public Result<UserInfoResponse> getCurrentUser() {
        SecurityUser userDetails = getCurrentUserDetails();
        UserInfoResponse userInfo = authService.getCurrentUser(userDetails.getUserId());
        return Result.success(userInfo);
    }

    @GetMapping("/sms-codes")
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
