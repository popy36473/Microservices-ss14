package com.example.identityservice.controller;

import com.example.identityservice.dto.request.RefreshTokenRequest;
import com.example.identityservice.dto.request.UserLogin;
import com.example.identityservice.dto.request.UserRegister;
import com.example.identityservice.dto.response.ApiResponse;
import com.example.identityservice.dto.response.JwtResponse;
import com.example.identityservice.entity.Users;
import com.example.identityservice.security.jwt.JwtProvider;
import com.example.identityservice.security.principal.UserDetailServiceCustom;
import com.example.identityservice.security.principal.UserPrincipal;
import com.example.identityservice.service.UserService;
import com.example.identityservice.service.impl.RefreshTokenService;
import com.example.identityservice.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor

public class AuthController {
    @Autowired
    private UserDetailServiceCustom customUserDetailsService;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private final UserServiceImpl userService;
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Users>> register(@RequestBody UserRegister userRegister){
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Đăng ký tài khoản " + userRegister.getUsername() + " thành công",
                userService.registerUser(userRegister),
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

    @GetMapping("/test-token")
    public ResponseEntity<String> testToken(@RequestParam String username) {
        // 1. Lấy thông tin user dạng CustomUserDetails
        UserPrincipal userDetails = (UserPrincipal) customUserDetailsService.loadUserByUsername(username);

        // 2. Tạo chuỗi token
        String token = jwtProvider.generateToken(userDetails);

        // 3. Trả về token
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLogin userLogin) {
        try {
            JwtResponse jwtResponse = userService.login(userLogin);
            return ResponseEntity.ok(jwtResponse); // Mã 200 OK
        } catch (BadCredentialsException e) {
            // Trường hợp nhập sai username hoặc password -> Trả về 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username hoặc password không đúng!");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            JwtResponse jwtResponse = refreshTokenService.refreshTokenRotation(request.getRefreshToken());
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        userService.logout(authHeader);
        return ResponseEntity.ok("Đăng xuất thành công!");
}
}

