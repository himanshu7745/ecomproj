package com.example.ecomproj.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "password_reset_attempts")
@Entity
public class PasswordResetAttempt {
    @Id
    @GeneratedValue
    private Long id;

    private String email;

    private int attempts;

    private LocalDateTime lastAttempt;
}
