package com.example.ecomproj.auth.dto.Requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignInRequest {
    @Email(message = "Invalid Email Format")
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Password is Required")
    private String password;
}
