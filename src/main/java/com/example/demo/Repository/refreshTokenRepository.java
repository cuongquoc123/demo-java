package com.example.demo.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entity.User;
import com.example.demo.Entity.refreshToken;

public interface refreshTokenRepository extends JpaRepository<refreshToken, Long> {

    public Optional<refreshToken> findByToken(String token);
    public Optional<refreshToken> findByUser(User user);
}
