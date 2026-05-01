package com.resale.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 *
 * @author MiniMax Agent
 */
@Data
public class LoginRequest {

    /**
     * 账号（用户名/手机/邮箱）
     */
    @NotBlank(message = "账号不能为空")
    private String account;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 图形验证码Key
     */
    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;

    /**
     * 用户输入的验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;

    /**
     * 是否记住我
     */
    private Boolean rememberMe = false;
}
