package com.example.bankcards.service;

import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            log.info("Authentication successful for user: {}", request.getUsername());

        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for user: {}", request.getUsername());
            throw ex; // 401
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        log.debug("Loaded userDetails: {}", userDetails.getUsername());

        String token;
        try {
            token = jwtService.generateAccessToken(userDetails);
            log.info("JWT generated for user: {}", request.getUsername());
        } catch (Exception ex) {
            log.error("JWT generation failed for user: {}", request.getUsername(), ex);
            throw ex;
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("User not found in DB after authentication: {}", request.getUsername());
                    return new RuntimeException("User not found");
                });

        log.info("Login successful for user: {}", user.getUsername());

        return new AuthResponse(
                "Bearer " + token,
                user.getUsername(),
                user.getRole().name()
        );
    }

}
