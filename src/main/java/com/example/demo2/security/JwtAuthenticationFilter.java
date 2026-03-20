package com.example.demo2.security;

import com.example.demo2.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        System.out.println("=== JWT Filter triggered for: " + request.getRequestURI());

        // Выводим все заголовки запроса
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        System.out.println("Headers received:");
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println("  " + headerName + ": " + request.getHeader(headerName));
        }

        try {
            String jwt = getJwtFromRequest(request);
            System.out.println("JWT from request: " + jwt);

            if (StringUtils.hasText(jwt) && tokenProvider.validateAccessToken(jwt)) {
                System.out.println("Token is valid");
                String username = tokenProvider.getUsernameFromAccessToken(jwt);
                System.out.println("Username from token: " + username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("Authentication set for user: " + username);
            } else {
                System.out.println("Token invalid or missing");
            }
        } catch (Exception ex) {
            System.out.println("Exception in JWT filter: " + ex.getMessage());
            ex.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}