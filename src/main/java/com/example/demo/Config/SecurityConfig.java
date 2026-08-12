package com.example.demo.Config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.example.demo.FillterChains.JwtFillter;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFillter jwtFillter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtFillter jwtFillter, UserDetailsService UserDetailsService) {
        this.jwtFillter = jwtFillter;
        this.userDetailsService = UserDetailsService;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFillter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                String origin = request.getHeader("Origin");

                // Cấu hình chung
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                // 1. Nếu là Admin: Cho phép truy cập mọi URL với tất cả các quyền
                if ("http://frontend-admin.com".equals(origin)) {
                    config.setAllowedOrigins(List.of("http://frontend-admin.com"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    return config;
                }
                // 2. Nếu là Reader: Chỉ được phép gọi GET vào /api/demos/**
                String uri = request.getRequestURI();
                if ("http://frontend-reader.com".equals(origin) && uri.startsWith("/api/demos/")) {
                    config.setAllowedOrigins(List.of("http://frontend-reader.com"));
                    config.setAllowedMethods(List.of("GET"));
                    return config;
                }
                return null; // Chặn các đối tượng còn lại
            }
        };
    }
}
