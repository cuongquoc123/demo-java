package com.example.demo.FillterChains;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.time.LocalDateTime;

import com.example.demo.dto.ErrorRespone;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;

@Component
public class RateLimitingFillter extends OncePerRequestFilter {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitingFillter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // Cấu hình giới hạn:
    // Đăng nhập: 100 req/phút mỗi endpoint
    private static final long AUTH_CAPACITY = 100;
    private static final long AUTH_REFILL_PER_MINUTE = 100;

    // Chưa đăng nhập: 10 req/phút mỗi endpoint
    private static final long ANON_CAPACITY = 10;
    private static final long ANON_REFILL_PER_MINUTE = 10;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String key;
        long capacity;
        long refillRate;

        // Lấy endpoint (Method + URI)
        String endpoint = request.getMethod() + ":" + request.getRequestURI();

        // Lấy thông tin xác thực từ SecurityContextHolder (đã được JwtFillter thiết lập trước đó)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null 
                && authentication.isAuthenticated() 
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            // Đã đăng nhập: Dùng Username + Endpoint làm khóa định danh
            String username = authentication.getName();
            key = "user:" + username + ":" + endpoint;
            capacity = AUTH_CAPACITY;
            refillRate = AUTH_REFILL_PER_MINUTE;
        } else {
            // Chưa đăng nhập: Dùng IP + Endpoint làm khóa định danh
            String ip = getClientIp(request);
            key = "anon:" + ip + ":" + endpoint;
            capacity = ANON_CAPACITY;
            refillRate = ANON_REFILL_PER_MINUTE;
        }

        // Lấy hoặc tạo mới Token Bucket cho key tương ứng
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(capacity, refillRate));

        if (bucket.tryConsume()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType("application/json;charset=UTF-8");
            ErrorRespone errorResponse = new ErrorRespone(
                    "Vượt quá giới hạn request cho phép. Vui lòng thử lại sau.",
                    429,
                    LocalDateTime.now(),
                    "Too Many Requests",
                    request.getRequestURI()
            );
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    // Lớp Token Bucket thread-safe
    private static class TokenBucket {
        private final long capacity;
        private final double refillRate; // tokens per millisecond
        private double tokens;
        private long lastRefillTimestamp;

        public TokenBucket(long capacity, long refillTokensPerMinute) {
            this.capacity = capacity;
            // refillRate = tokens / millisecond
            this.refillRate = (double) refillTokensPerMinute / 60000.0;
            this.tokens = capacity;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsedTime = now - lastRefillTimestamp;
            if (elapsedTime > 0) {
                double tokensToAdd = elapsedTime * refillRate;
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTimestamp = now;
            }
        }
    }
}
