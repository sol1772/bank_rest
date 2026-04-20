package com.example.bankcards.service.validators;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserValidator implements Validator {
    private final UserRepository userRepository;

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return User.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        User user = (User) target;

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return;
        }

        userRepository.findByUsername(user.getUsername())
                .ifPresent(existingUser -> {
                    if (user.getId() == null || !existingUser.getId().equals(user.getId())) {
                        String message = String.format("Username is already in use: %s", user.getUsername());
                        errors.rejectValue("username", "username.taken", message);
                        log.warn("Duplicate username attempt: {}", user.getUsername());
                    }
                });
    }
}