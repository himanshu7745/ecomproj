package com.example.ecomproj.Repository;

import com.example.ecomproj.entity.Users;
import com.example.ecomproj.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepo extends JpaRepository<VerificationToken,Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken>findByUser(Users user);
}
