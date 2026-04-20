package com.example.bankcards.controller;

import com.example.bankcards.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

public class CustomSecurityMockMvcRequestPostProcessors {
    public static RequestPostProcessor customUser(Long id, String username) {
        CustomUserDetails user = new CustomUserDetails(
                id,
                username,
                "password",
                List.of(new SimpleGrantedAuthority("USER"))
        );

        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                .authentication(
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()
                        )
                );
    }
}
