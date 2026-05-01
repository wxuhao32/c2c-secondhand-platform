package com.resale.platform.config;

import com.resale.platform.common.AuditLog;
import com.resale.platform.entity.AuditLogEntry;
import com.resale.platform.mapper.AuditLogMapper;
import com.resale.platform.security.SecurityUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogMapper auditLogMapper;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        int statusCode = 200;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            statusCode = 500;
            throw e;
        } finally {
            try {
                long duration = System.currentTimeMillis() - startTime;
                saveAuditLog(joinPoint, auditLog, statusCode, duration);
            } catch (Exception e) {
                log.error("保存审计日志失败", e);
            }
        }
    }

    private void saveAuditLog(ProceedingJoinPoint joinPoint, AuditLog auditLog, int statusCode, long duration) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setModule(auditLog.module());
        entry.setAction(auditLog.action());
        entry.setDescription(auditLog.description());
        entry.setMethod(((MethodSignature) joinPoint.getSignature()).getMethod().getName());
        entry.setStatusCode(statusCode);
        entry.setDuration(duration);
        entry.setCreatedAt(LocalDateTime.now());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof SecurityUser user) {
            entry.setUserId(user.getUserId());
            entry.setUsername(user.getUsername());
        }

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            entry.setPath(request.getRequestURI());
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }
            entry.setIp(ip);
        }

        try {
            auditLogMapper.insert(entry);
        } catch (Exception e) {
            log.warn("审计日志写入失败（可能表不存在）: {}", e.getMessage());
        }
    }
}
