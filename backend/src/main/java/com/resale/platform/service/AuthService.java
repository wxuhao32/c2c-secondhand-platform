package com.resale.platform.service;

import com.resale.platform.dto.request.LoginRequest;
import com.resale.platform.dto.request.RegisterRequest;
import com.resale.platform.dto.response.LoginResponse;

/**
 * 认证服务接口
 *
 * @author MiniMax Agent
 */
public interface AuthService {

    /**
     * 账号密码登录
     *
     * @param request 登录请求
     * @param ipAddress 登录IP
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request, String ipAddress);

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 用户ID
     */
    Long register(RegisterRequest request);

    /**
     * 登出
     *
     * @param token 用户Token
     */
    void logout(String token);

    /**
     * 微信OAuth登录
     *
     * @param code 微信授权码
     * @param state 状态参数
     * @return 登录响应
     */
    LoginResponse wechatLogin(String code, String state);

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID
     * @return 用户信息响应
     */
    com.resale.platform.dto.response.UserInfoResponse getCurrentUser(Long userId);
}
