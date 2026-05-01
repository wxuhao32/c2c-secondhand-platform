package com.resale.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 *
 * @author MiniMax Agent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT访问令牌
     */
    private String token;

    /**
     * Token类型
     */
    private String tokenType;

    /**
     * 有效期（秒）
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserInfoResponse userInfo;

    /**
     * 是否为新用户（仅短信登录时使用）
     */
    private Boolean isNewUser;
}
