package com.example.bankcards.service;

import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.request.LoginRequest;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * Service for authentication operations.
 */
public interface AuthService {
    /**
     * Authenticates user and returns JWT token.
     *
     * @param request login request containing credentials
     * @return authentication response with token
     * @throws BadCredentialsException if credentials are invalid
     */
    AuthResponse login(LoginRequest request);
}
