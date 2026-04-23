package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.mappers.UserMapper;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasAuthority('ADMIN')")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with id %d does not exist", id)));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<UserDto> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    public UserDto getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return userMapper.toDto(user);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public User createUser(User user, String newPassword) {
        Objects.requireNonNull(user, "User must not be null");
        Assert.hasText(newPassword, "New password must not be null or empty");
        if (user.getId() != null) {
            user.setId(null);
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        User saved = userRepository.save(user);
        log.info("New user created with id {}", saved.getId());
        return saved;
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(String.format("User with id %d does not exist", id));
        }
        userRepository.deleteById(id);
        log.info("User with id {} is deleted", id);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public User changeRole(Long id, Role newRole) {
        User user = userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with id %d does not exist", id)));
        user.setRole(newRole);
        User updated = userRepository.save(user);
        log.info("Role changed to {} for user {}", newRole, id);
        return updated;
    }

    @Transactional
    @PreAuthorize("#id == authentication.principal.id")
    public User changePassword(Long id, String oldPassword, String newPassword) {
        Assert.hasText(newPassword, "New password must not be null or empty");
        User user = userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppRuntimeException(
                        String.format("User with id %d does not exist", id)));
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            log.warn("Password change failed for user {} — old password incorrect", id);
            throw new AppRuntimeException("Old password is incorrect!");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        User updated = userRepository.save(user);
        log.info("Password changed successfully for user {}", id);
        return updated;
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public User resetPassword(Long id, String newPassword) {
        Assert.hasText(newPassword, "New password must not be null or empty");
        User user = userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppRuntimeException(
                        String.format("User with id %d does not exist", id)));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        User updated = userRepository.save(user);
        log.info("Password reset by admin for user {}", id);
        return updated;
    }
}
