package com.example.identityservice.service;

import com.example.identityservice.dto.request.UserLogin;
import com.example.identityservice.dto.request.UserRegister;
import com.example.identityservice.dto.response.JwtResponse;
import com.example.identityservice.entity.Users;

import java.util.List;

public interface UserService {
    List<Users> findAll();
    Users findById(Long id);
    Users insert(Users user);
    Users update(Long userId, Users user);
    void deleteById(Long id);
    Users registerUser(UserRegister  userRegister);
    JwtResponse login(UserLogin userLogin);

}
