package com.example.bankcards.service.validators;

import com.example.bankcards.entity.BankTransaction;
import jakarta.annotation.Nonnull;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

@Service
@NoArgsConstructor
@Transactional(readOnly = true)
public class BankTransactionValidator implements Validator {
    private static final Logger logger = LoggerFactory.getLogger(BankTransactionValidator.class);

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return BankTransaction.class.equals(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        BankTransaction bankTransaction = (BankTransaction) target;
        if (bankTransaction.getFromCardId() == null) {
            String str = String.format("Bank transaction has no source-card id: %s", bankTransaction);
            errors.rejectValue("fromCard", "", str);
            logger.warn(str);
        }
        if (bankTransaction.getToCardId() == null) {
            String str = String.format("Bank transaction has no destination-card id: %s", bankTransaction);
            errors.rejectValue("toCard", "", str);
            logger.warn(str);
        }
        if (bankTransaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            String str = String.format("The transaction amount must be greater than zero: %s", bankTransaction);
            errors.rejectValue("amount", "", str);
            logger.warn(str);
        }
    }
}
