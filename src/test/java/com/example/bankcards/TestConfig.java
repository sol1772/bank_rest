package com.example.bankcards;

import com.example.bankcards.dto.mappers.BankTransactionMapper;
import com.example.bankcards.dto.mappers.CardMapper;
import com.example.bankcards.dto.mappers.UserMapper;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.*;
import com.example.bankcards.service.validators.BankTransactionValidator;
import com.example.bankcards.service.validators.CardValidator;
import com.example.bankcards.service.validators.UserValidator;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {
    @Bean
    public UserService userService() {
        return Mockito.mock(UserService.class);
    }

    @Bean
    public CardService cardService() {
        return Mockito.mock(CardService.class);
    }

    @Bean
    public BankTransactionService bankTransactionService() {
        return Mockito.mock(BankTransactionService.class);
    }

    @Bean
    public CardBlockingRequestService blockingRequestService() {
        return Mockito.mock(CardBlockingRequestService.class);
    }

    @Bean
    public AuthService authService() {
        return Mockito.mock(AuthService.class);
    }

    @Bean
    public UserValidator userValidator() {
        return Mockito.mock(UserValidator.class);
    }

    @Bean
    public CardValidator cardValidator() {
        return Mockito.mock(CardValidator.class);
    }

    @Bean
    public BankTransactionValidator bankTransactionValidator() {
        return Mockito.mock(BankTransactionValidator.class);
    }

    @Bean
    public UserMapper userMapper() {
        return Mockito.mock(UserMapper.class);
    }

    @Bean
    public CardMapper cardMapper() {
        return Mockito.mock(CardMapper.class);
    }

    @Bean
    public BankTransactionMapper transactionMapper() {
        return Mockito.mock(BankTransactionMapper.class);
    }

    @Bean
    public JwtService jwtService() {
        return Mockito.mock(JwtService.class);
    }

    @Bean
    public CustomUserDetailsService userDetailsService() {
        return Mockito.mock(CustomUserDetailsService.class);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return Mockito.mock(JwtAuthenticationFilter.class);
    }
}