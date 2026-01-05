package com.example.ecomproj.Repository;

import com.example.ecomproj.entity.PasswordResetAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetAttemptRepository extends JpaRepository<PasswordResetAttempt,Long> {

    Optional<PasswordResetAttempt> findByEmail(String email);
}
