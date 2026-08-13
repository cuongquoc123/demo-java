package com.example.demo.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Repository.refreshTokenRepository;
import com.example.demo.Service.JWTService;
import com.example.demo.Service.RefeshTokenService;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.TokenRefreshResponse;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.Entity.User;
import com.example.demo.Entity.refreshToken;
import com.example.demo.Repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthControllers {
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final RefeshTokenService refreshTokenService;
    private final refreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthControllers(AuthenticationManager authenticationManager,
            JWTService jwtService,
            RefeshTokenService refreshTokenService,
            refreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        // 2. Nếu xác thực thành công, tạo Access Token và Refresh Token cho Client
        String accessToken = jwtService.GeneratedToken(request.getUsername());
        refreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken.getToken());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username đã tồn tại!"));
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Đăng ký thành công!"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getToken();

        // Gọi qua biến đối tượng đã được tiêm ở trên
        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration) // Kiểm tra hết hạn
                .map(token -> token.getUser()) // Lấy ra User sở hữu token
                .map(user -> {
                    // của class
                    String newAccessToken = jwtService.GeneratedToken(user.getUsername());

                    // Trả về Access Token mới cho Client
                    return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, requestRefreshToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh Token không tồn tại trong hệ thống!"));
    }
}
