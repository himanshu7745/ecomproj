package com.example.ecomproj.auth.dto.Responses;

import com.example.ecomproj.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String message;
    private String access_token;
    private String refresh_token;
    private String email;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private Role role;
}
