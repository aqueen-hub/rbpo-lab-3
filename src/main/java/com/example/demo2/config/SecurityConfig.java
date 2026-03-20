package com.example.demo2.config;

import com.example.demo2.security.JwtAuthenticationFilter;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
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
                .csrf(csrf -> csrf.disable())  // отключаем CSRF для API
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // без сессий
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register").permitAll() // регистрация без токена
                        .requestMatchers("/api/auth/login").permitAll()    // логин без токена
                        .requestMatchers("/api/auth/refresh").permitAll()  // обновление токена без токена
                        // Остальные эндпоинты требуют аутентификации и ролей (как было)
                        .requestMatchers("/api/members/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/trainers/**").hasRole("ADMIN")
                        .requestMatchers("/api/lessons/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/subscriptions/**").hasRole("ADMIN")
                        .requestMatchers("/api/bookings/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // наш фильтр

        return http.build();
    }
}