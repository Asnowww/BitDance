package com.bitdance.iam.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BEARER = "Bearer ";
    private static final String SET_PASSWORD_PATH = "/auth/password";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER)) {
            try {
                Claims claims = jwtService.parse(header.substring(BEARER.length()));
                long userId = Long.parseLong(claims.getSubject());
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.getOrDefault("roles", List.of());
                boolean passwordRequired = Boolean.TRUE.equals(claims.get("passwordRequired", Boolean.class));
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                        .collect(Collectors.toList())
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                if (passwordRequired && !isSetPasswordRequest(request)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"code\":\"PASSWORD_REQUIRED\",\"message\":\"请先设置登录密码\",\"data\":null}"
                    );
                    return;
                }
            } catch (JwtException | IllegalArgumentException ex) {
                log.debug("Reject invalid JWT: {}", ex.getMessage());
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isSetPasswordRequest(HttpServletRequest request) {
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());
        return "POST".equalsIgnoreCase(request.getMethod()) && SET_PASSWORD_PATH.equals(path);
    }
}
