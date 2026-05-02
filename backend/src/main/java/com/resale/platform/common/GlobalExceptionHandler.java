package com.resale.platform.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: uri={}, code={}, message={}", request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.fail(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验异常: uri={}, message={}", request.getRequestURI(), message);
        return Result.error(ExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定异常: uri={}, message={}", request.getRequestURI(), message);
        return Result.error(ExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(), message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleMissingParameter(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数: uri={}, param={}", request.getRequestURI(), e.getParameterName());
        return Result.error(ExceptionEnum.BAD_REQUEST.getCode(), "缺少必要参数: " + e.getParameterName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体解析失败: uri={}, error={}", request.getRequestURI(), e.getMessage());
        return Result.error(ExceptionEnum.BAD_REQUEST.getCode(), "请求体格式错误，请检查JSON格式");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型转换失败: uri={}, param={}, value={}", request.getRequestURI(), e.getName(), e.getValue());
        return Result.error(ExceptionEnum.BAD_REQUEST.getCode(), "参数格式错误: " + e.getName());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持: uri={}, method={}", request.getRequestURI(), e.getMethod());
        return Result.error(ExceptionEnum.BAD_REQUEST.getCode(), "不支持的请求方法: " + e.getMethod());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBadCredentials(BadCredentialsException e, HttpServletRequest request) {
        log.warn("认证失败: uri={}, message={}", request.getRequestURI(), e.getMessage());
        return Result.error(ExceptionEnum.PASSWORD_ERROR.getCode(), "用户名或密码错误");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证异常: uri={}, type={}, message={}", request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage());
        return Result.error(ExceptionEnum.UNAUTHORIZED.getCode(), "认证失败，请重新登录");
    }

    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleSQLException(SQLException e, HttpServletRequest request) {
        log.error("数据库异常: uri={}, error={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ExceptionEnum.INTERNAL_ERROR.getCode(), "数据库操作失败，请稍后再试");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("参数非法: uri={}, message={}", request.getRequestURI(), e.getMessage());
        return Result.error(ExceptionEnum.BAD_REQUEST.getCode(), e.getMessage() != null ? e.getMessage() : "参数错误");
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常: uri={}, trace={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ExceptionEnum.INTERNAL_ERROR.getCode(), "系统处理异常，请联系管理员");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: uri={}, type={}, message={}", request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage(), e);
        String message = determineRuntimeErrorMessage(e);
        return Result.error(ExceptionEnum.INTERNAL_ERROR.getCode(), message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: uri={}, type={}, message={}", request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage(), e);
        return Result.error(ExceptionEnum.INTERNAL_ERROR.getCode(), "系统异常，请稍后再试");
    }

    private String determineRuntimeErrorMessage(RuntimeException e) {
        String className = e.getClass().getSimpleName();
        if (className.contains("Timeout") || className.contains("timeout")) {
            return "请求超时，请检查网络后重试";
        }
        if (className.contains("Connection") || className.contains("connection")) {
            return "网络连接异常，请稍后再试";
        }
        if (e.getMessage() != null) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("timeout")) {
                return "请求超时，请稍后再试";
            }
            if (msg.contains("connection")) {
                return "网络连接异常，请稍后再试";
            }
            if (msg.contains("database") || msg.contains("sql")) {
                return "数据库操作失败，请稍后再试";
            }
        }
        return "系统繁忙，请稍后再试";
    }
}
