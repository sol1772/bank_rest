package com.example.bankcards.service.validators;

import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.YearMonth;

@Service
@NoArgsConstructor
@AllArgsConstructor
@Transactional(readOnly = true)
public class CardValidator implements Validator {
    private static final Logger logger = LoggerFactory.getLogger(UserValidator.class);
    private CardRepository cardRepository;

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return Card.class.equals(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        Card card = (Card) target;
        if (cardRepository.findByNumber(card.getNumber()) != null) {
            String str = String.format("Card number is already in use: %s", card.getNumber());
            errors.rejectValue("number", "", str);
            logger.warn(str);
        }
        if (card.getExpiry().isBefore(YearMonth.now())) {
            String str = String.format("Card expiration date is invalid: %s", card.getExpiry());
            errors.rejectValue("expirationDate", "", str);
            logger.warn(str);
        }
    }
}
