package com.codearena.repository;

import com.codearena.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
