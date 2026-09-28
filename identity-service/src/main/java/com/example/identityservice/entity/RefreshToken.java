package com.example.identityservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token; // Chuỗi UUID

    @Column(nullable = false)
    private Instant expiryDate; // Thời gian hết hạn

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Users user; // Mối quan hệ với Entity Users
}