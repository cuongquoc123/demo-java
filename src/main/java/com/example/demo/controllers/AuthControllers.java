package com.example.demo.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Service.JWTService;
import com.example.demo.dto.LoginRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthControllers {
    
    private AuthenticationManager authenticationManager;
    private JWTService JwtService;

    public AuthControllers(AuthenticationManager authenticationManager, JWTService jwtService) {
        this.authenticationManager = authenticationManager;
        this.JwtService = jwtService;
    }

    public ResponseEntity<Map<String,String>> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        // 2. Nếu xác thực thành công, tạo Token và trả về cho Client
        String token = JwtService.GeneratedToken(request.getUsername());
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        
        return ResponseEntity.ok(response);
    }

}
