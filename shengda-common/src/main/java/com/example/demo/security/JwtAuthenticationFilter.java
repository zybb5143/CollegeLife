package com.example.demo.security;

import com.example.demo.model.User;
import com.example.demo.service.UserStatusService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 认证过滤器
 * 每个请求都会经过此过滤器，验证 Token 并设置安全上下文
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;  // JWT 工具类，用于验证和解析 Token

    @Autowired
    private UserStatusService userStatusService;  // 从 Redis 读取用户登录状态

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 步骤 1：从请求头中获取 Token，并验证是否有效
            String token = getJwtFromRequest(request);

            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 步骤 2：从 Token 中提取用户名
                String username = jwtUtil.getUsernameFromToken(token);

                // 步骤 3：从 Redis 判断用户是否处于登录状态，未登录/已过期/已退出则不予认证
                if (userStatusService.isOnline(username)) {
                    // 步骤 4：创建用户对象（角色从 Token 解析，供 @PreAuthorize 鉴权使用）
                    User user = new User();
                    user.setUserName(username);
                    user.setRole(jwtUtil.getRoleFromToken(token));

                    // 步骤 5：创建认证对象并存入当前线程的安全上下文
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        // 步骤 6：继续执行后续的过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 JWT Token
     * 格式：Authorization: Bearer <token>
     * 
     * @param request HTTP 请求
     * @return Token 字符串，如果没有则返回 null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 从 Authorization 请求头获取 Token
        String bearerToken = request.getHeader("Authorization");
        
        // 检查 Token 是否存在且以 "Bearer " 开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 截取 "Bearer " 之后的部分（从索引 7 开始）
            return bearerToken.substring(7);
        }
        return null;
    }
}
