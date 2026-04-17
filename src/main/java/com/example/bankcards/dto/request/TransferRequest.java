package com.example.bankcards.dto.request;

import com.example.bankcards.entity.Card;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotNull(message = "Source card is required")
    private Card fromCard;

    @NotNull(message = "Destination card is required")
    private Card toCard;

    @DecimalMin(value = "0.01", message = "Amount cannot be zero or negative")
    private BigDecimal amount;
}
