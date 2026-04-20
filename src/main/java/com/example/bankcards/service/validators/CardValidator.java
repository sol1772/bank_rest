package com.example.bankcards.service.validators;

import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.YearMonth;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CardValidator implements Validator {
    private final CardRepository cardRepository;

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return Card.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        Card card = (Card) target;
        if (cardRepository.findByNumber(card.getNumber()).isPresent()) {
            String message = String.format("Card number is already in use: %s", card.getNumber());
            errors.rejectValue("number", "", message);
            log.warn(message);
        }
        if (card.getExpiry().isBefore(YearMonth.now())) {
            String message = String.format("Card expiration date is invalid: %s", card.getExpiry());
            errors.rejectValue("expiry", "", message);
            log.warn(message);
        }
    }
}
