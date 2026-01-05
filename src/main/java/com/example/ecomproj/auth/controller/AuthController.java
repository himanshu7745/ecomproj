package com.example.ecomproj.auth.controller;

import com.example.ecomproj.auth.dto.Requests.*;
import com.example.ecomproj.auth.dto.Responses.*;
import com.example.ecomproj.auth.service.AuthService;
import com.example.ecomproj.entity.Role;
import com.example.ecomproj.entity.Users;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/auth")
@Validated
public class AuthController {
    private final AuthService authService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResponse> register(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam Role role,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        SignUpRequest dto = new SignUpRequest(firstName,lastName,email,password,role);
        RegisterResponse response = authService.register(dto,file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(
            @RequestParam("token")
            @NotBlank String token
    ) {
        MessageResponse response = authService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody SignInRequest request){
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request){
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(Authentication authentication){
        return ResponseEntity.ok(authService.logout(authentication.getName(),null));
    }

    @PutMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateProfileResponse> updateProfile(
            Authentication authentication,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException{
        String tokenEmail = authentication.getName();

        UpdateProfileResponse response = authService.updateProfile(
                tokenEmail,
                firstName,
                lastName,
                file
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<GetProfileResponse> getCurrentUser(Authentication authentication){
        GetProfileResponse response = authService.getUserProfile(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<MessageResponse> deleteCurrentUser(Authentication authentication){
        MessageResponse response = authService.deleteCurrentUser(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-email")
    public ResponseEntity<UpdateEmailRequest> requestEmailUpdate(
            @Valid @RequestBody UpdateEmailRequest request,
            Authentication authentication
    ){
        UpdateEmailRequest response = authService.requestEmailUpdate(authentication.getName(),request.getNewEmail());
        return  ResponseEntity.accepted().body(response);
    }

    @GetMapping("/update-email/verify")
    public ResponseEntity<UpdateEmailResponse> verifyUpdatedEmail(@RequestParam("token") @NotBlank String token){
        UpdateEmailResponse response = authService.verifyEmailUpdate(token);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-password")
    public ResponseEntity<MessageResponse> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            Authentication authentication
    ){
        return ResponseEntity.ok(authService.updatePassword(request,authentication.getName()));
    }

    @PutMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody Map<String,String>body){
        return ResponseEntity.ok(authService.forgotPassword(body.get("email")));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @GetMapping("/dev/users")
    public ResponseEntity<List<Users>> listUsers(){
        return ResponseEntity.ok(authService.getAllUsers());
    }


}
