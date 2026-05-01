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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        log.debug("JwtAuthenticationFilter - 处理路径: {}", path);

        boolean isPublicPath = path.contains("/auth/captcha") ||
               path.contains("/auth/login") ||
               path.contains("/auth/register") ||
               path.contains("/auth/sendSms") ||
               path.contains("/auth/loginBySms") ||
               path.contains("/auth/sms-codes") ||
               path.contains("/auth/forgotPassword") ||
               path.contains("/auth/wechat/") ||
               path.equals("/api/products") && "GET".equals(request.getMethod()) ||
               path.contains("/products/categories") ||
               path.contains("/products/category/") ||
               path.matches(".*/products/\\d+$") ||
               path.contains("/search/");

        if (isPublicPath) {
            log.debug("JwtAuthenticationFilter - 公开接口，设置匿名认证并放行: {}", path);
            UsernamePasswordAuthenticationToken anonymousAuth =
                    new UsernamePasswordAuthenticationToken("anonymous", null, java.util.Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String tokenId = tokenProvider.getTokenId(jwt);
                boolean isBlacklisted = tokenBlacklistService.isBlacklisted(tokenId);

                if (!isBlacklisted) {
                    Long userId = tokenProvider.getUserIdFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserById(userId);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            log.error("无法设置用户认证", ex);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
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
