package com.example.ecomproj.auth.service;

import com.example.ecomproj.entity.Users;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface JwtService {
    String generateToken(Users user);

    String generateRefreshToken(Users user);

    String extractUsername(String token);

    boolean validateToken(String token, UserDetails userDetails);

    List<String> extractRoles(String token);
}
