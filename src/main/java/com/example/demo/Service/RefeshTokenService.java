package com.example.demo.Service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.demo.Entity.refreshToken;
import com.example.demo.Repository.*;

import com.example.demo.Entity.User;

@Service
public class RefeshTokenService {
   
    private final refreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefeshTokenService(refreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    } 

    // 1. Tạo hoặc cập nhật Refresh Token mới cho User dựa trên username
    public refreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));

        refreshToken token = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> {
                    refreshToken newToken = new refreshToken();
                    newToken.setUser(user);
                    return newToken;
                });

        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 7)); // 7 ngày
        return refreshTokenRepository.save(token);
    }

    // 1b. Tạo Refresh Token mới cho User bằng userId
    public refreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với id: " + userId));

        refreshToken token = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> {
                    refreshToken newToken = new refreshToken();
                    newToken.setUser(user);
                    return newToken;
                });

        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 7)); // 7 ngày
        return refreshTokenRepository.save(token);
    }
    // 2. Kiểm tra xem Token dưới DB đã hết hạn chưa
    public refreshToken verifyExpiration(refreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token); // Xóa khỏi DB nếu hết hạn
            throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại!");
        }
        return token;
    }
}
