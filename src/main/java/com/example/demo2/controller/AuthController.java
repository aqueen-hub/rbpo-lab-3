package com.example.demo2.controller;

import com.example.demo2.dto.*;
import com.example.demo2.model.User;
import com.example.demo2.repository.UserRepository;
import com.example.demo2.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(TokenService tokenService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = tokenService.authenticate(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Invalid username or password"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        try {
            LoginResponse response = tokenService.refreshTokens(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Invalid refresh token"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Проверка существования пользователя
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Username already exists"));
        }

        // Проверка надёжности пароля
        if (!isPasswordStrong(request.getPassword())) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "Password must be at least 8 characters, contain uppercase, lowercase, digit and special character"));
        }

        // Создание пользователя
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(List.of("USER"));
        userRepository.save(user);

        return ResponseEntity.ok(new RegisterResponse("User registered successfully", user.getUsername()));
    }

    private boolean isPasswordStrong(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return Pattern.matches(pattern, password);
    }
}