package com.example.bankcards.service.validators;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@NoArgsConstructor
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserValidator implements Validator {
    private static final Logger logger = LoggerFactory.getLogger(UserValidator.class);
    private UserRepository userRepository;

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        User user = (User) target;
        if (userRepository.findByUsername(user.getUsername()) != null) {
            String str = String.format("Username is already in use: %s", user.getUsername());
            errors.rejectValue("login", "", str);
            logger.warn(str);
        }
    }
}
