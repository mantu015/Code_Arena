package com.codearena.service;

import com.codearena.model.User;
import com.codearena.repository.SubmissionRepository;
import com.codearena.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private UserRepository userRepository;
    private EmailVerificationService emailVerificationService;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        emailVerificationService = mock(EmailVerificationService.class);
        passwordEncoder = new BCryptPasswordEncoder(4);
        userService = new UserService(userRepository, mock(SubmissionRepository.class), emailVerificationService, passwordEncoder);
    }

    @Test
    void rejectsRegistrationPasswordsMissingRequiredCharacterTypes() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.verifyAndRegister("user@example.com", "123456", "password1!"));
        assertThrows(IllegalArgumentException.class,
                () -> userService.verifyAndRegister("user@example.com", "123456", "Password!"));
        assertThrows(IllegalArgumentException.class,
                () -> userService.verifyAndRegister("user@example.com", "123456", "Password1"));
        verifyNoInteractions(emailVerificationService, userRepository);
    }

    @Test
    void registersOnlyAfterEmailCodeIsConsumedAndPasswordIsStrong() {
        when(emailVerificationService.consumeVerification("user@example.com", "123456"))
                .thenReturn("user@example.com");
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.verifyAndRegister("user@example.com", "123456", "Password1!");

        assertEquals("user@example.com", result.getUsername());
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("Password1!", result.getPassword()));
        org.junit.jupiter.api.Assertions.assertNotEquals("Password1!", result.getPassword());
        verify(emailVerificationService).consumeVerification("user@example.com", "123456");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void upgradesLegacyPlaintextPasswordAfterSuccessfulLogin() {
        User user = new User();
        user.setUsername("user@example.com");
        user.setPassword("OldPassword1!");
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.of(user));

        userService.login("user@example.com", "OldPassword1!");

        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("OldPassword1!", user.getPassword()));
        verify(userRepository).save(user);
    }

    @Test
    void leavesSeededAdminPasswordCompatibleWithAdminLogin() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setRole("ADMIN");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        userService.login("admin", "admin123");

        assertEquals("admin123", admin.getPassword());
        verify(userRepository, never()).save(any(User.class));
    }
}
