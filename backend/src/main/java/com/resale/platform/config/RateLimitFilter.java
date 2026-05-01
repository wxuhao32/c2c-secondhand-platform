package com.resale.platform.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;

    private static final Map<String, String> PATH_GROUP_MAPPING = Map.of(
            "/auth", "auth",
            "/products", "product",
            "/orders", "order"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!rateLimitConfig.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = getClientId(request);
        String apiGroup = resolveApiGroup(request.getRequestURI());
        String bucketKey = clientId + ":" + apiGroup;

        Bucket bucket = rateLimitConfig.resolveBucket(bucketKey);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for client: {}, path: {}", clientId, request.getRequestURI());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(429);
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"timestamp\":" + System.currentTimeMillis() + "}");
        }
    }

    private String getClientId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return "token:" + auth.substring(7, Math.min(auth.length(), 20));
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return "ip:" + forwarded.split(",")[0].trim();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private String resolveApiGroup(String path) {
        String apiPath = path.replace("/api", "");
        for (Map.Entry<String, String> entry : PATH_GROUP_MAPPING.entrySet()) {
            if (apiPath.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "default";
    }
}
