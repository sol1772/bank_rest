package com.example.bankcards.service;

import com.example.bankcards.dto.PageRequestDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PageRequestDto pageRequestDto = new PageRequestDto();

    @PreAuthorize("hasAuthority('ADMIN')")
    public User getUserById(Long id) {
        return userRepository.findUserById(id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<User> getAll() {
        pageRequestDto.setSortByColumn("username");
        Pageable pageable = pageRequestDto.getPageable();
        return userRepository.findAll(pageable);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public User createUser(User user, String newPassword) {
        assert user != null;
        if (user.getId() != null) {
            user.setId(null);
        }
        if (!newPassword.isBlank()) {
            user.setPasswordHash(newPassword);
            User dbUser = userRepository.save(user);
            if (logger.isInfoEnabled()) {
                logger.info("New user created {}", dbUser);
            }
            return dbUser;
        } else {
            throw new IllegalArgumentException("The password cannot be empty");
        }
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public User updateUser(User user) {
        Optional<User> updated = userRepository.findById(user.getId()).map(oldUser -> userRepository.save(user));
        if (updated.isPresent()) {
            if (logger.isInfoEnabled()) {
                updated.ifPresent(n -> logger.info("User {} updated", updated.get()));
            }
        } else {
            throw new AppRuntimeException(String.format("User with id %d does not exist", user.getId()));
        }
        return updated.get();
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new AppRuntimeException(String.format("User with id %d does not exist", id));
        }
        userRepository.deleteById(id);
        if (logger.isInfoEnabled()) {
            logger.info("User with id {} is deleted", id);
        }
    }

    @Transactional
    public User changePassword(Long id, String oldPassword, String newPassword) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            if (user.get().passwordIsValid(oldPassword)) {
                user.get().setPasswordHash(newPassword);
                User updated = userRepository.save(user.get());
                if (logger.isInfoEnabled()) {
                    logger.info("Password changed successfully for {}", user);
                }
                return updated;
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Password change error for {} (old password is incorrect)", user);
                }
                throw new AppRuntimeException("Old password is incorrect!");
            }
        } else {
            throw new AppRuntimeException(String.format("User with id %d does not exist", id));
        }
    }
}
