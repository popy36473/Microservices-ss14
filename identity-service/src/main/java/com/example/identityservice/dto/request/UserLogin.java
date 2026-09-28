package com.example.identityservice.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserLogin {
    private String username;
    private String password;
}
