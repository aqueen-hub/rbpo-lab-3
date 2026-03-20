package com.example.demo2.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    // Секретные ключи и время жизни подтягиваются из application.properties
    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.access.expiration}")
    private Long accessExpiration; // в миллисекундах

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration;

    // Метод возвращает ключ для подписи access токена (на основе секрета)
    private Key getAccessKey() {
        return Keys.hmacShaKeyFor(accessSecret.getBytes());
    }

    // Метод возвращает ключ для подписи refresh токена
    private Key getRefreshKey() {
        return Keys.hmacShaKeyFor(refreshSecret.getBytes());
    }

    // Генерация access токена на основе объекта Authentication (который содержит пользователя)
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), getAccessKey(), accessExpiration);
    }

    // Генерация refresh токена по имени пользователя (без пароля)
    public String generateRefreshToken(String username) {
        return generateToken(username, getRefreshKey(), refreshExpiration);
    }

    // Общий метод генерации токена
    private String generateToken(String subject, Key key, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setSubject(subject)               // subject = username
                .setIssuedAt(now)                  // время создания
                .setExpiration(expiryDate)         // время истечения
                .signWith(key, SignatureAlgorithm.HS256) // подпись
                .compact();
    }

    // Извлечь имя пользователя из access токена
    public String getUsernameFromAccessToken(String token) {
        return getClaimFromToken(token, getAccessKey(), Claims::getSubject);
    }

    // Извлечь имя пользователя из refresh токена
    public String getUsernameFromRefreshToken(String token) {
        return getClaimFromToken(token, getRefreshKey(), Claims::getSubject);
    }

    // Универсальный метод извлечения claim из токена
    private <T> T getClaimFromToken(String token, Key key, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }

    // Парсинг токена и получение всех claims
    private Claims extractAllClaims(String token, Key key) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // Проверка валидности access токена
    public boolean validateAccessToken(String token) {
        return validateToken(token, getAccessKey());
    }

    // Проверка валидности refresh токена
    public boolean validateRefreshToken(String token) {
        return validateToken(token, getRefreshKey());
    }

    // Общая проверка: подпись, срок действия, корректность
    private boolean validateToken(String token, Key key) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Геттер для времени жизни refresh токена (нужен для сохранения в базу)
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
}