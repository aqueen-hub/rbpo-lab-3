package com.example.demo2.config;

import com.example.demo2.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Отключаем CSRF для API
                .authorizeHttpRequests(auth -> auth
                        // Разрешаем регистрацию без аутентификации
                        .requestMatchers("/api/auth/register").permitAll()
                        // Примеры ограничений по ролям:
                        .requestMatchers("/api/members/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/trainers/**").hasRole("ADMIN")
                        .requestMatchers("/api/lessons/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/subscriptions/**").hasRole("ADMIN")
                        .requestMatchers("/api/bookings/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/bookings/book").hasRole("USER") // особая операция
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {}) // Включаем Basic Auth
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .userDetailsService(userDetailsService);

        return http.build();
    }
}