package com.example.ecomproj.Repository;

import com.example.ecomproj.entity.OTP;
import com.example.ecomproj.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OTP,Long> {

    void deleteByUser(Users user);

    Optional<OTP> findByUserAndOtp(Users user, String otp);
}
