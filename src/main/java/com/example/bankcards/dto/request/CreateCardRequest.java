package com.example.bankcards.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
public class CreateCardRequest {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^\\d{16}$", message = "Card number must be exactly 16 digits")
    private String number;

    @NotNull(message = "Holder ID is required")
    private Long holderId;

    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    private YearMonth expiry;

    @DecimalMin(value = "0.00", message = "Initial balance cannot be negative")
    private BigDecimal balance = BigDecimal.ZERO;
}
