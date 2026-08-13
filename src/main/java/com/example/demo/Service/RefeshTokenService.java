package com.example.demo.Service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.demo.Entity.refreshToken;
import com.example.demo.Repository.*;

@Service
public class RefeshTokenService {
   
    private refreshTokenRepository refreshTokenRepository;

    private UserRepository userRepository;

    public RefeshTokenService(refreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    } 
    // 1. Tạo Refresh Token mới cho User
    public refreshToken createRefreshToken(Long userId) {
        refreshToken refreshToken = new refreshToken();
        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 7)); // 7 ngày
        refreshToken.setToken(UUID.randomUUID().toString()); // Sinh chuỗi ngẫu nhiên
        return refreshTokenRepository.save(refreshToken);
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
