package com.example.identityservice.dto.request;

import com.example.identityservice.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserRegister {
    private String username;
    private String password;
    private List<Role> roles;
}
