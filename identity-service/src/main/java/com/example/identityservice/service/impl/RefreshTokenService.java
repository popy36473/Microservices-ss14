package com.example.identityservice.service.impl;

import com.example.identityservice.dto.response.JwtResponse;
import com.example.identityservice.entity.RefreshToken;
import com.example.identityservice.entity.Users;
import com.example.identityservice.repository.RefreshTokenRepository;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.security.jwt.JwtProvider;
import com.example.identityservice.security.principal.UserDetailServiceCustom;
import com.example.identityservice.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-token-expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private  final UserDetailServiceCustom userDetailServiceCustom;
    private final JwtProvider jwtProvider;

    // 1. Hàm tạo Refresh Token và lưu vào DB
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user id: " + userId));

        // Xóa token cũ nếu có
        refreshTokenRepository.deleteByUser(user);

        // Sinh UUID ngẫu nhiên và tính expiryDate
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // 2. Hàm kiểm tra Refresh Token đã hết hạn chưa
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại!");
        }
        return token;
    }

    @Transactional
    public JwtResponse refreshTokenRotation(String requestToken) {
        // 1. Tìm RefreshToken trong DB
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại trong DB!"));

        // 2. Kiểm tra xem token đã hết hạn chưa
        verifyExpiration(refreshToken);

        // 3. (QUAN TRỌNG - Rotation): Xóa bản ghi RefreshToken cũ khỏi DB
        Users user = refreshToken.getUser();
        refreshTokenRepository.delete(refreshToken);

        // 4. Lấy CustomUserDetails và tạo Access Token MỚI
        UserPrincipal userDetails = (UserPrincipal) userDetailServiceCustom.loadUserByUsername(user.getUsername());
        String newAccessToken = jwtProvider.generateToken(userDetails);

        // 5. Tạo Refresh Token MỚI HOÀN TOÀN (Lưu DB)
        RefreshToken newRefreshToken = createRefreshToken(user.getUserId());

        // 6. Trả về cặp Token mới cho Client
        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .username(user.getUsername())
                .build();
    }
}