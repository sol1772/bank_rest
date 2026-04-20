package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class CardDto {
    private final String number;
    private final UserDto holder;
    private final YearMonth expiry;
    private Long id;
    private CardStatus status;
    private BigDecimal balance;
}
