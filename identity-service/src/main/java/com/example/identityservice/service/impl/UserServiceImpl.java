package com.example.identityservice.service.impl;

import com.example.identityservice.dto.request.UserLogin;
import com.example.identityservice.dto.request.UserRegister;
import com.example.identityservice.dto.response.JwtResponse;
import com.example.identityservice.entity.RefreshToken;
import com.example.identityservice.entity.Users;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.security.jwt.JwtProvider;
import com.example.identityservice.security.principal.UserDetailServiceCustom;
import com.example.identityservice.security.principal.UserPrincipal;
import com.example.identityservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailServiceCustom customUserDetailsService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final StringRedisTemplate redisTemplate;

    @Override
    public List<Users> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Users findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Không tồn tại user " + id));
    }

    @Override
    public Users insert(Users user) {
        return userRepository.save(user);
    }

    @Override
    public Users update(Long userId, Users user) {
        userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Không tồn tại user " + userId));
        user.setUserId(userId);
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Không tồn tại user " + id));
        userRepository.deleteById(id);
    }

    @Override
    public Users registerUser(UserRegister userRegister) {
        Users user = Users.builder()
                .username(userRegister.getUsername())
                .password(passwordEncoder.encode(userRegister.getPassword()))
                .roles(userRegister.getRoles())
                .build();
        return userRepository.save(user);
    }

    @Override
    public JwtResponse login(UserLogin userLogin) {
        // 1. Xác thực username / password
        Users user = userRepository.findByUsername(userLogin.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        if (!passwordEncoder.matches(userLogin.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Bad credentials");
        }

        // 2. Gọi JwtProvider tạo Access Token
        UserPrincipal userDetails = (UserPrincipal) customUserDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtProvider.generateToken(userDetails);

        // 3. Gọi RefreshTokenService tạo Refresh Token (Lưu DB)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUserId());

        // 4. Trả về DTO chứa cả hai Token
        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .username(user.getUsername())
                .build();
    }

    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);

            // 1. Giải mã lấy "jti" và "exp"
            String jti = jwtProvider.getJtiFromToken(accessToken);
            Date expiration = jwtProvider.getExpirationFromToken(accessToken);

            // 2. Tính toán TTL (Time-To-Live) còn lại của Token (milisecond)
            long ttl = expiration.getTime() - System.currentTimeMillis();

            if (ttl > 0) {
                // 3. Lưu key "blacklist:<jti>" vào Redis với giá trị "revoked" và thời gian hết hạn TTL
                redisTemplate.opsForValue().set(
                        "blacklist:" + jti,
                        "revoked",
                        ttl,
                        TimeUnit.MILLISECONDS
                );
            }
        }
    }

}
