package com.example.ecomproj.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String token;

    @Column(name = "new_email")
    private String newEmail;

    @OneToOne
    @JoinColumn(name = "user_id",referencedColumnName = "id",nullable = false,unique = true)
    private Users user;

    private LocalDateTime expiryDate;

    private boolean used = false;
}
