package com.example.bankcards.service;

import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("login: should authenticate and return JWT")
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testUser");
        req.setPassword("Password1");

        User user = User.builder()
                .id(1L).username("testUser")
                .role(Role.USER).passwordHash("hashed")
                .build();

        UserDetails ud = new org.springframework.security.core.userdetails.User(
                "testUser", "hashed", List.of(new SimpleGrantedAuthority("USER")));

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userDetailsService.loadUserByUsername("testUser")).thenReturn(ud);
        when(jwtService.generateAccessToken(ud)).thenReturn("login-jwt");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("Bearer login-jwt");
        assertThat(response.getUsername()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("login: should throw BadCredentialsException for wrong password")
    void login_badCredentials() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testUser");
        req.setPassword("WrongPass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }
}