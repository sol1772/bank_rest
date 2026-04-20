package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BankTransactionDto {
    private final String transactionReference;
    private Long id;
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
