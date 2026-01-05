package com.example.ecomproj.auth.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.ecomproj.Repository.OtpRepository;
import com.example.ecomproj.Repository.PasswordResetAttemptRepository;
import com.example.ecomproj.Repository.UsersRepo;
import com.example.ecomproj.Repository.VerificationTokenRepo;
import com.example.ecomproj.auth.dto.Requests.*;
import com.example.ecomproj.auth.dto.Responses.*;
import com.example.ecomproj.auth.exceptions.*;
import com.example.ecomproj.auth.service.AuthService;
import com.example.ecomproj.auth.service.JwtService;
import com.example.ecomproj.entity.*;
import com.example.ecomproj.exceptions.InvalidTokenException;
import com.example.ecomproj.exceptions.ResourceNotFoundException;
import com.example.ecomproj.util.EmailService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;
    private final VerificationTokenRepo verificationTokenRepo;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordResetAttemptRepository resetAttemptRepository;
    private final OtpRepository otpRepository;


    @Override
    @Transactional
    public RegisterResponse register(SignUpRequest signUpRequest, MultipartFile file) throws IOException{

        if(usersRepo.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException("The new email is already in use");
        }

        Role userRole = signUpRequest.getRole() != null ? signUpRequest.getRole() : Role.ROLE_USER;

        Users user = Users.builder()
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .email(signUpRequest.getEmail())
                .role(userRole)
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .build();

        if(file != null && !file.isEmpty()){
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "Customers",
                            "public_id", signUpRequest.getEmail(),
                            "overwrite", true,
                            "resource_type","image"
                    )
            );
            String imageUrl = (String) uploadResult.get("secure_url");
            user.setProfileImageUrl(imageUrl);
        }

        usersRepo.save(user);

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationTokenRepo.save(verificationToken);

        // send verification email (change the URL depending on your deployment(frontend(3000) or backend(8080)) )
        String verificationLink = "http://localhost:8080/api/v1/auth/verify-email?token=" + token;
        String body = "Hello " + user.getFirstName() + ",\n\n" +
                "Click the link to verify your account:\n" + verificationLink +
                "\n\nIf you did not register, ignore this email.";

        emailService.sendMail(user.getEmail(),"Verification Email",body);

        return new RegisterResponse("User Registered. Please Check email for verification.", token);
    }

    @Override
    @Transactional
    public MessageResponse verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepo.findByToken(token)
                .orElseThrow(()-> new IllegalArgumentException("Invalid Token"));

        Users user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEnabled(true);
        usersRepo.save(user);
        verificationTokenRepo.save(verificationToken);

        return new MessageResponse("Email Verified Successful");
    }

    @Override
    public LoginResponse login(SignInRequest signInRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signInRequest.getEmail(), signInRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid Email or password");
        }

        Users user = usersRepo.findByEmail(signInRequest.getEmail())
                .orElseThrow(()-> new InvalidCredentialsException("Invalid Email or password"));

        if(!user.isEnabled()){
            throw new AccountNotVerifiedException("Email is not verified. Verify your email before login.");
        }

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new LoginResponse(
               "Login Successful",
                token,
                refreshToken,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getProfileImageUrl(),
                user.getRole()
        );
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String email;

        try {
            email = jwtService.extractUsername(refreshTokenRequest.getToken());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Users user = usersRepo.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User", "email", email));

        if(!jwtService.validateToken(refreshTokenRequest.getToken(),user)){
            throw new InvalidTokenException("Refresh Token Expired or invalid");
        }

        String newAccessToken = jwtService.generateToken(user);

        return  new LoginResponse(
                "Token Refreshed Successfully",
                newAccessToken,
                refreshTokenRequest.getToken(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getProfileImageUrl(),
                user.getRole()
        );
    }

    @Override
    public MessageResponse logout(String email, String refreshToken) {
        return new MessageResponse("Logged Out successfully");
    }

    @Override
    public UpdateProfileResponse updateProfile(
            String tokenEmail,
            String firstName,
            String lastName,
            MultipartFile file
    ) throws IOException {
        Users user = usersRepo.findByEmail(tokenEmail)
                .orElseThrow(()-> new UserNotFoundException("User Not Found"));
        if(firstName != null ) user.setFirstName(firstName);
        if(lastName != null ) user.setLastName(lastName);

        if(file != null && !file.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder","Customers",
                            "public_id", tokenEmail,
                            "overwrite", "true",
                            "resource_type","image"
                    ));
            String imageUrl = (String) uploadResult.get("secure_url");
            user.setProfileImageUrl(imageUrl);
        }
        usersRepo.save(user);
        return new UpdateProfileResponse(
                "Profile Updated Successfully",
                user.getFirstName(),
                user.getLastName(),
                user.getProfileImageUrl()
        );
    }

    @Override
    public GetProfileResponse getUserProfile(String email) {
        Users users = usersRepo.findByEmail(email)
                .orElseThrow(()-> new UserNotFoundException("User not found with email: " + email));
        return new GetProfileResponse(
                users.getFirstName(),
                users.getLastName(),
                users.getEmail(),
                users.getProfileImageUrl()
        );
    }

    @Override
    @Transactional
    public MessageResponse deleteCurrentUser(String email) {
        Users users = usersRepo.findByEmail(email)
                .orElseThrow(()-> new UserNotFoundException("User not found with email: " + email));
        usersRepo.delete(users);
        return new MessageResponse("Email Deleted Successfully");
    }

    @Override
    @Transactional
    public UpdateEmailRequest requestEmailUpdate(String currentEmail, String newEmail) {
        Users user = usersRepo.findByEmail(currentEmail)
                .orElseThrow(()-> new UserNotFoundException("User not found with email: " + currentEmail));

        if(usersRepo.findByEmail(newEmail).isPresent()){
            throw new EmailAlreadyUsedException("The new email already exists");
        }
        VerificationToken token = verificationTokenRepo.findByUser(user)
                .orElse(new VerificationToken());
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusHours(24));
        token.setUsed(false);
        token.setNewEmail(newEmail);
        verificationTokenRepo.save(token);

        // Send email with token link
        String verificationLink = "http://localhost:8080/api/v1/auth/update-email/verify?token=" + token;
        String body = "Hello " + user.getFirstName() + ",\n\n" +
                "Click the link to verify your account:\n" + verificationLink +
                "\n\nIf you did not try to change your email , ignore this email.";
        emailService.sendMail(newEmail, "Verify your email", "Click to verify: " + body);

        return new UpdateEmailRequest(
                "Verification email sent. Please check your email to confirm new email",
                newEmail,
                token.getToken()
        );

    }

    @Override
    @Transactional
    public UpdateEmailResponse verifyEmailUpdate(String token) {
        VerificationToken token1 = verificationTokenRepo.findByToken(token)
                .orElse(new VerificationToken());
        if(token1.isUsed()){
            throw new InvalidTokenException("This token is already used");
        }
        if(token1.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new InvalidTokenException("Token is expired");
        }

        Users user = token1.getUser();
        user.setEmail(token1.getNewEmail());
        token1.setUsed(true);
        usersRepo.save(user);
        verificationTokenRepo.save(token1);
        return new UpdateEmailResponse(
                "Email updated successfully",
                user.getEmail()
        );
    }

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(String email) {
        Users user = usersRepo.findByEmail(email)
                .orElseThrow(()-> new InvalidCredentialsException("User not found with email: " + email));

        PasswordResetAttempt resetAttempt = resetAttemptRepository.findByEmail(email)
                .orElse(new PasswordResetAttempt());

        if(resetAttempt.getAttempts() >= 3){
            throw new TooManyRequestsException("Too many reset attempts. Try again later.");
        }

        resetAttempt.setEmail(email);
        resetAttempt.setAttempts(resetAttempt.getAttempts() + 1);
        resetAttempt.setLastAttempt(LocalDateTime.now());
        resetAttemptRepository.save(resetAttempt);

        String otp = String.format("%06d", new Random().nextInt(1_000_000));

        otpRepository.deleteByUser(user);

        OTP otpEntity = new OTP();
        otpEntity.setUser(user);
        otpEntity.setOtp(otp);
        otpEntity.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        otpRepository.save(otpEntity);

        String body = "Hello " + user.getFirstName() + ",\n\n" +
                "Your password reset code (OTP) is: " + otp + "\n\n" +
                "This code expires in 15 minutes.";
        emailService.sendMail(user.getEmail(), "Password Reset OTP", body);

        return new ForgotPasswordResponse(
                "Otp sent successfully",
                otp
        );

    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        Users user = usersRepo.findByEmail(request.getEmail())
                .orElseThrow(()-> new InvalidCredentialsException("User not found with email: " + request.getEmail()));

        OTP otpEntity = otpRepository.findByUserAndOtp(user,request.getOtp())
                .orElseThrow(()-> new InvalidOtpException("Invalid OTP"));

        if(otpEntity.getExpiryDate().isBefore(LocalDateTime.now())){
            otpRepository.delete(otpEntity);
            throw new InvalidOtpException("OTP is expired");
        }

        user.setPassword(request.getPassword());
        usersRepo.save(user);

        otpRepository.save(otpEntity);

        return new MessageResponse("Password reset successfully");

    }

    @Override
    public List<Users> getAllUsers() {
        return usersRepo.findAll();
    }

    @Override
    public MessageResponse updatePassword(UpdatePasswordRequest request, String email) {
        Users user = usersRepo.findByEmail(email)
                .orElseThrow(()-> new UserNotFoundException("User not found with email: " + email));

        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            throw new IllegalArgumentException("Old password doesn't match");
        }

        user.setPassword(request.getNewPassword());
        usersRepo.save(user);
        return new MessageResponse("Password updated successfully");
    }


}
