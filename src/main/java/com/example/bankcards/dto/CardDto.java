package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
public class CardDto {
    private final String number;
    private final UserDto holder;
    private final YearMonth expirationDate;
    private Long id;
    private CardStatus status;
    private BigDecimal balance;
}
