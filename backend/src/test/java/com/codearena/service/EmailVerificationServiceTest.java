package com.codearena.service;

import com.codearena.model.EmailVerification;
import com.codearena.repository.EmailVerificationRepository;
import com.codearena.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailVerificationServiceTest {
    @Test
    void emailsAOneTimeCodeAndAcceptsItOnlyForThatEmail() {
        EmailVerificationRepository verificationRepository = mock(EmailVerificationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        beanFactory.addBean("mailSender", mailSender);
        EmailVerificationService service = new EmailVerificationService(
                verificationRepository,
                userRepository,
                beanFactory.getBeanProvider(JavaMailSender.class),
                passwordEncoder);
        ReflectionTestUtils.setField(service, "mailHost", "smtp.example.com");
        ReflectionTestUtils.setField(service, "mailFrom", "noreply@example.com");

        AtomicReference<EmailVerification> storedVerification = new AtomicReference<>();
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.empty());
        when(verificationRepository.findById("user@example.com")).thenReturn(Optional.empty());
        when(verificationRepository.save(any(EmailVerification.class))).thenAnswer(invocation -> {
            EmailVerification verification = invocation.getArgument(0);
            storedVerification.set(verification);
            return verification;
        });
        doAnswer(invocation -> {
            SimpleMailMessage message = invocation.getArgument(0);
            String code = extractCode(message.getText());
            assertTrue(passwordEncoder.matches(code, storedVerification.get().getCodeHash()));
            return null;
        }).when(mailSender).send(any(SimpleMailMessage.class));

        service.requestVerification("User@Example.com");

        verify(mailSender).send(any(SimpleMailMessage.class));
        assertEquals("user@example.com", storedVerification.get().getEmail());
        String code = extractCode(captureMessageText(mailSender));
        when(verificationRepository.findById("user@example.com"))
                .thenReturn(Optional.of(storedVerification.get()));

        assertEquals("user@example.com", service.consumeVerification("USER@example.com", code));
        verify(verificationRepository).delete(storedVerification.get());
    }

    private String captureMessageText(JavaMailSender mailSender) {
        var messageCaptor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        return messageCaptor.getValue().getText();
    }

    private String extractCode(String message) {
        Matcher matcher = Pattern.compile("code is (\\d{6})").matcher(message);
        if (!matcher.find()) {
            throw new AssertionError("Verification email did not contain a six-digit code.");
        }
        return matcher.group(1);
    }
}
