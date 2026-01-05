package com.example.ecomproj.auth.dto.Requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEmailRequest {
    private String message;
    @Email(message = "Not a valid email")
    @NotBlank(message = "New email is required")
    private String newEmail;
    @NotBlank(message = "verification token is required")
    private String verificationToken;
}
