package com.example.demo2.controller;

import com.example.demo2.dto.RegisterRequest;
import com.example.demo2.dto.RegisterResponse;
import com.example.demo2.model.User;
import com.example.demo2.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        // Проверка на существование пользователя
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Проверка надёжности пароля
        String password = request.getPassword();
        if (!isPasswordStrong(password)) {
            throw new RuntimeException("Password must be at least 8 characters long, contain uppercase, lowercase, digit and special character");
        }

        // Создаём нового пользователя с ролью USER (можно добавить ADMIN отдельно)
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(List.of("USER")); // роль по умолчанию

        userRepository.save(user);

        return new RegisterResponse("User registered successfully", user.getUsername());
    }

    private boolean isPasswordStrong(String password) {
        // Минимум 8 символов, хотя бы одна заглавная, одна строчная, одна цифра, один спецсимвол
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(pattern);
    }
}