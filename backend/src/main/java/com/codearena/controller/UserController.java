package com.codearena.controller;

import com.codearena.model.User;
import com.codearena.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> requestRegistrationCode(@Valid @RequestBody EmailRequest request) {
        try {
            userService.requestRegistrationVerification(request.getEmail());
            return ResponseEntity.accepted().body(Map.of("message", "Verification code sent. Check your email inbox."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmailAndRegister(@Valid @RequestBody VerificationRequest request) {
        try {
            User user = userService.verifyAndRegister(request.getEmail(), request.getCode(), request.getPassword());
            return ResponseEntity.ok(Map.of("id", user.getId(), "username", user.getUsername()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            User user = userService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getUserStats(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserStats(id));
    }
}

@Data
class AuthRequest {
    private String username;
    private String password;
}

@Data
class EmailRequest {
    @NotBlank
    @Email
    private String email;
}

@Data
class VerificationRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String code;
    @NotBlank
    private String password;
}
