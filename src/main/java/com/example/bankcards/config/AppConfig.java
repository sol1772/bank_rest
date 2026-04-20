package com.example.bankcards.config;

import com.example.bankcards.security.JwtService;
import com.example.bankcards.util.CryptoUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppConfig {
    @Value("${ENCRYPTION_KEY}")
    private String cardKey;

    @Value("${JWT_SECRET}")
    private String jwtKey;

    @Value("${KEY_SALT}")
    private String keySalt;

    @Value("${application.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${application.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Bean
    public CryptoUtil cryptoUtil() {
        return new CryptoUtil(CryptoUtil.getSecretKey(getCardKey(), getKeySalt()));
    }

    @Bean
    public JwtService jwtService() {
        return new JwtService(JwtService.jwtSecretKey(getJwtKey()),
                getAccessTokenExpiration(), getRefreshTokenExpiration());
    }
}
