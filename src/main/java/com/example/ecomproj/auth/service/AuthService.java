package com.example.ecomproj.auth.service;

import com.example.ecomproj.auth.dto.Requests.*;
import com.example.ecomproj.auth.dto.Responses.*;
import com.example.ecomproj.entity.Users;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AuthService {
    RegisterResponse register(SignUpRequest signUpRequest , MultipartFile file) throws IOException;

    MessageResponse verifyEmail(@NotBlank String token);

    LoginResponse login(@Valid SignInRequest request);

    LoginResponse refreshToken(@Valid RefreshTokenRequest refreshTokenRequest);

    MessageResponse logout(String email, String refreshToken);

    UpdateProfileResponse updateProfile(String tokenEmail, String firstName, String lastName, MultipartFile file) throws IOException;

    GetProfileResponse getUserProfile(String email);

    MessageResponse deleteCurrentUser(String email);

    UpdateEmailRequest requestEmailUpdate(String currentEmail, @Email(message = "Not a valid email") @NotBlank(message = "New email is required") String newEmail);

    UpdateEmailResponse verifyEmailUpdate(@NotBlank String token);

    ForgotPasswordResponse forgotPassword(String email);

    MessageResponse resetPassword(ResetPasswordRequest request);

    List<Users> getAllUsers();

    MessageResponse updatePassword(@Valid UpdatePasswordRequest request, String email);
}
