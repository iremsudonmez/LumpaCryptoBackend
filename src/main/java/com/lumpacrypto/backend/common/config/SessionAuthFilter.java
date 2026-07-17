package com.lumpacrypto.backend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumpacrypto.backend.common.error.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// checks the bearer token on protected routes -> puts userId into the request
@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    public static final String USER_ID_ATTR = "authenticatedUserId";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public SessionAuthFilter(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // public routes -> no token needed
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api/v1/market/prices");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            reject(response);
            return;
        }

        String token = header.substring(7);
        String userId = redis.opsForValue().get("session:" + token);

        if (userId == null) { // unknown or expired -> redis ttl already cleaned it
            reject(response);
            return;
        }

        request.setAttribute(USER_ID_ATTR, UUID.fromString(userId));
        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(
                ErrorResponse.of("Session is missing or expired", "INVALID_TOKEN")));
    }
}