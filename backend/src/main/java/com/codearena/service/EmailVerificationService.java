package com.codearena.service;

import com.codearena.model.EmailVerification;
import com.codearena.repository.EmailVerificationRepository;
import com.codearena.repository.UserRepository;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Transactional
    public void requestVerification(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.findByUsername(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailHost.isBlank() || mailFrom.isBlank() || mailSender == null) {
            throw new IllegalStateException("Email verification is not configured. Please contact the administrator.");
        }

        LocalDateTime now = LocalDateTime.now();
        verificationRepository.deleteByExpiresAtBefore(now);
        EmailVerification current = verificationRepository.findById(normalizedEmail).orElse(null);
        if (current != null && current.getCreatedAt().isAfter(now.minusSeconds(60))) {
            throw new IllegalArgumentException("Please wait one minute before requesting another verification code.");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        EmailVerification verification = new EmailVerification();
        verification.setEmail(normalizedEmail);
        verification.setCodeHash(passwordEncoder.encode(code));
        verification.setCreatedAt(now);
        verification.setExpiresAt(now.plusMinutes(10));
        verification.setAttempts(0);
        verificationRepository.save(verification);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(normalizedEmail);
        message.setSubject("Your Code Arena verification code");
        message.setText("Your Code Arena verification code is " + code
                + ". It expires in 10 minutes. If you did not request this, you can ignore this email.");
        try {
            mailSender.send(message);
        } catch (MailException | IllegalStateException exception) {
            verificationRepository.deleteById(normalizedEmail);
            throw new IllegalStateException("Could not send the verification email. Please check the mail settings and try again.", exception);
        }
    }

    public String consumeVerification(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        EmailVerification verification = verificationRepository.findById(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Request a new email verification code and try again."));

        LocalDateTime now = LocalDateTime.now();
        if (verification.getExpiresAt().isBefore(now)) {
            verificationRepository.delete(verification);
            throw new IllegalArgumentException("The verification code expired. Request a new code.");
        }

        if (verification.getAttempts() >= MAX_ATTEMPTS
                || !passwordEncoder.matches(code, verification.getCodeHash())) {
            verification.setAttempts(verification.getAttempts() + 1);
            if (verification.getAttempts() >= MAX_ATTEMPTS) {
                verificationRepository.delete(verification);
            } else {
                verificationRepository.save(verification);
            }
            throw new IllegalArgumentException("Invalid verification code.");
        }

        verificationRepository.delete(verification);
        return normalizedEmail;
    }

    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        try {
            InternetAddress address = new InternetAddress(normalized);
            address.validate();
            if (!address.getAddress().equals(normalized) || !normalized.contains(".")) {
                throw new IllegalArgumentException("Enter a valid email address.");
            }
        } catch (AddressException exception) {
            throw new IllegalArgumentException("Enter a valid email address.", exception);
        }
        return normalized;
    }

}
