package com.example.demo.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {
    private final String Secret_key = "day_la_khoa_bi_mat_sieu_bao_mat_cua_toi_viet_bang_tieng_viet";

    private SecretKey getSigningKey() {
        byte[] keyBytes = Secret_key.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String GeneratedToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // Hạn dùng 24 giờ
                .signWith(getSigningKey())
                .compact();
    }

    public String ExtractUsername(String Token) {
        return extractClaim(Token, claims -> claims.getSubject());
    }

    public boolean IsValidToken(String Token, UserDetails userDetails) {
        String username = ExtractUsername(Token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(Token));
    }


    private boolean isTokenExpired(String token) {
        return extractClaim(token, claims -> claims.getExpiration()).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
