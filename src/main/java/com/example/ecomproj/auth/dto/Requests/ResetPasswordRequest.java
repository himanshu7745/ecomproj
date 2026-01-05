package com.example.ecomproj.auth.dto.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    private String email;
    @NotBlank(message = "otp is required")
    private String otp;
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password should be of size 8")
    private String password;

}
