package com.resale.platform.common;

import lombok.Getter;

/**
 * 异常枚举类
 *
 * @author MiniMax Agent
 */
@Getter
public enum ExceptionEnum {

    // 通用异常 (1000-1099)
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请登录"),
    FORBIDDEN(403, "无权访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 认证异常 (2000-2099)
    ACCOUNT_NOT_FOUND(2001, "账号不存在"),
    PASSWORD_ERROR(2002, "密码错误"),
    ACCOUNT_LOCKED(2003, "账户已被锁定"),
    ACCOUNT_DISABLED(2004, "账户已被禁用"),
    CAPTCHA_ERROR(2005, "验证码错误"),
    CAPTCHA_EXPIRED(2006, "验证码已过期"),
    CAPTCHA_REQUIRED(2007, "请先获取验证码"),
    SMS_CODE_ERROR(2008, "短信验证码错误"),
    SMS_CODE_EXPIRED(2009, "短信验证码已过期"),
    SMS_SEND_TOO_FREQUENT(2010, "发送太频繁，请稍后再试"),
    SMS_SEND_LIMIT_EXCEEDED(2011, "短信发送次数超限"),
    SMS_CODE_INVALID(2012, "短信验证码无效或已过期"),
    TOKEN_INVALID(2013, "Token无效"),
    TOKEN_EXPIRED(2014, "Token已过期"),
    LOGIN_FAILED_TOO_MANY(2015, "登录失败次数过多，请15分钟后再试"),

    // 用户异常 (3000-3099)
    USERNAME_EXIST(3001, "用户名已存在"),
    MOBILE_EXIST(3002, "手机号已存在"),
    EMAIL_EXIST(3003, "邮箱已存在"),
    USER_NOT_FOUND(3004, "用户不存在"),
    OLD_PASSWORD_ERROR(3005, "原密码错误"),
    PASSWORD_FORMAT_ERROR(3006, "密码格式不正确"),

    // 微信登录异常 (4000-4099)
    WECHAT_LOGIN_ERROR(4001, "微信登录失败"),
    WECHAT_BIND_EXIST(4002, "该微信已绑定其他账户"),

    // 业务异常 (5000-5099)
    PARAMETER_VALIDATION_ERROR(5001, "参数校验失败"),
    OPERATION_TOO_FREQUENT(5002, "操作太频繁");

    private final Integer code;
    private final String message;

    ExceptionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
