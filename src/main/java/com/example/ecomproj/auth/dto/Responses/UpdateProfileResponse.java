package com.example.ecomproj.auth.dto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileResponse {
    private String message;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
}
