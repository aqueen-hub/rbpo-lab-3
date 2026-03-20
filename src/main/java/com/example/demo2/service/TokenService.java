package com.example.demo2.service;

import com.example.demo2.dto.LoginResponse;
import com.example.demo2.model.SessionStatus;
import com.example.demo2.model.User;
import com.example.demo2.model.UserSession;
import com.example.demo2.repository.UserRepository;
import com.example.demo2.repository.UserSessionRepository;
import com.example.demo2.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final CustomUserDetailsService userDetailsService;

    public TokenService(AuthenticationManager authenticationManager,
                        JwtTokenProvider tokenProvider,
                        UserRepository userRepository,
                        UserSessionRepository sessionRepository,
                        CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public LoginResponse authenticate(String username, String password) {
        // Аутентификация через AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // Генерация токенов
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(username);

        // Находим пользователя в БД
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Вычисляем срок жизни refresh токена
        LocalDateTime expiresAt = LocalDateTime.now()
                .plus(tokenProvider.getRefreshExpiration(), ChronoUnit.MILLIS);

        // Сохраняем сессию
        UserSession session = new UserSession(user, refreshToken, expiresAt);
        sessionRepository.save(session);

        // Логирование для отладки
        System.out.println("Saved session for user " + username + " with refresh token: " + refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public LoginResponse refreshTokens(String oldRefreshToken) {
        System.out.println("Refresh attempt with token: " + oldRefreshToken);

        // Проверка валидности refresh токена
        if (!tokenProvider.validateRefreshToken(oldRefreshToken)) {
            System.out.println("Refresh token is invalid");
            throw new RuntimeException("Invalid refresh token");
        }

        // Поиск сессии по токену
        UserSession session = sessionRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        System.out.println("Session found: id=" + session.getId() + ", status=" + session.getStatus());

        // Проверка статуса сессии
        if (session.getStatus() != SessionStatus.ACTIVE) {
            System.out.println("Session is not active: " + session.getStatus());
            throw new RuntimeException("Session is not active");
        }

        // Проверка срока действия
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            sessionRepository.save(session);
            throw new RuntimeException("Refresh token expired");
        }

        User user = session.getUser();

        // Загружаем UserDetails с ролями через сервис
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        // Генерируем новую пару токенов
        String newAccessToken = tokenProvider.generateAccessToken(auth);
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        // Деактивируем старую сессию
        session.setStatus(SessionStatus.INACTIVE);
        session.setRevokedAt(LocalDateTime.now());
        sessionRepository.save(session);

        // Создаём новую сессию
        LocalDateTime expiresAt = LocalDateTime.now()
                .plus(tokenProvider.getRefreshExpiration(), ChronoUnit.MILLIS);
        UserSession newSession = new UserSession(user, newRefreshToken, expiresAt);
        sessionRepository.save(newSession);

        System.out.println("Refresh successful, new refresh token: " + newRefreshToken);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }
}