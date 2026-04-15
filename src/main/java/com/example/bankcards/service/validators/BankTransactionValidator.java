package com.example.bankcards.service.validators;

import com.example.bankcards.entity.BankTransaction;
import jakarta.annotation.Nonnull;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

@Component
@NoArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BankTransactionValidator implements Validator {
    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return BankTransaction.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        BankTransaction bankTransaction = (BankTransaction) target;
        if (bankTransaction.getFromCard() == null) {
            String str = String.format("Bank transaction has no source-card: %s", bankTransaction);
            errors.rejectValue("fromCard", "", str);
            log.warn(str);
        }
        if (bankTransaction.getToCard() == null) {
            String str = String.format("Bank transaction has no destination-card: %s", bankTransaction);
            errors.rejectValue("toCard", "", str);
            log.warn(str);
        }
        if (bankTransaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            String str = String.format("The transaction amount must be greater than zero: %s", bankTransaction);
            errors.rejectValue("amount", "", str);
            log.warn(str);
        }
    }
}
