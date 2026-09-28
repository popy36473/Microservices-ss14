package com.example.identityservice.repository;

import com.example.identityservice.entity.RefreshToken;
import com.example.identityservice.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);


    void deleteByUser(Users user);
}