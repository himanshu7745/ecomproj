package com.example.ecomproj.auth.dto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponse {
    private String message;
    private String verificationToken;
    private String profileImageUrl;

    public RegisterResponse(String message){
        this.message = message;
    }

    public RegisterResponse(String message, String verificationToken){
        this.message = message;
        this.verificationToken = verificationToken;
    }
}
