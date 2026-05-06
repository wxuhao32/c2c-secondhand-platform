package com.resale.platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.resale.platform.service.impl.TokenBlacklistService;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final Set<String> PUBLIC_PATH_PREFIXES = Set.of(
            "/auth/captcha",
            "/auth/login",
            "/auth/register",
            "/auth/sendSms",
            "/auth/loginBySms",
            "/auth/sms-codes",
            "/auth/forgotPassword",
            "/auth/wechat/",
            "/products/categories",
            "/products/category/",
            "/search/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String fullPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = (contextPath != null && !contextPath.isEmpty() && fullPath.startsWith(contextPath))
                ? fullPath.substring(contextPath.length())
                : fullPath;
        String method = request.getMethod();

        if (isPublicPath(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String tokenId = tokenProvider.getTokenId(jwt);

                if (!tokenBlacklistService.isBlacklisted(tokenId)) {
                    Long userId = tokenProvider.getUserIdFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserById(userId);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            log.error("无法设置用户认证: path={}, error={}", path, ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path, String method) {
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }

        if ("GET".equals(method) && path.equals("/products")) {
            return true;
        }

        if ("GET".equals(method) && path.matches(".*/products/\\d+$")) {
            return true;
        }

        if ("GET".equals(method) && path.matches(".*/comments/.*")) {
            return true;
        }

        return false;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
