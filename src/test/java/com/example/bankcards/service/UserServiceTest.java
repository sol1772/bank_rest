package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_encodesPassword_andSavesUser() {
        // given
        User user = new User();

        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        userService.createUser(user, "secret");

        // then
        verify(passwordEncoder).encode("secret");
        verify(userRepository).save(user);
    }

    @Test
    void createUser_shouldThrowException_whenPasswordEmpty() {
        // given
        User user = new User();
        String password = "";

        // when & then
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user, password));

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldChangePasswordSuccessfully() {
        // given
        Long holderId = 1L;
        User user = new User();
        user.setId(holderId);
        user.setPasswordHash("oldHashed");

        String oldPassword = "oldPass";
        String newPassword = "newPass";

        when(userRepository.findByIdForUpdate(holderId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq(oldPassword), anyString())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("newHashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        User updated = userService.changePassword(holderId, oldPassword, newPassword);

        // then
        assertNotNull(updated);
        assertEquals("newHashed", user.getPasswordHash());

        verify(userRepository).findByIdForUpdate(holderId);
        verify(passwordEncoder).matches(oldPassword, "oldHashed");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_shouldThrowException_whenOldPasswordIncorrect() {
        // given
        Long id = 1L;
        User user = new User();
        user.setId(id);
        user.setPasswordHash("oldHashed");

        when(userRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(eq("wrongOld"), anyString()))
                .thenReturn(false);

        // when & then
        assertThrows(AppRuntimeException.class,
                () -> userService.changePassword(id, "wrongOld", "newPass"));

        verify(passwordEncoder).matches("wrongOld", "oldHashed");
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldThrowException_whenNewPasswordNull() {
        // given
        Long id = 1L;

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(id, "oldPass", null));

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_shouldResetPasswordSuccessfully() {
        // given
        Long holderId = 1L;
        User user = new User();
        user.setId(holderId);

        String newPassword = "newPass";

        when(userRepository.findByIdForUpdate(holderId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("newHashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        User updated = userService.resetPassword(holderId, newPassword);

        // then
        assertNotNull(updated);
        assertEquals("newHashed", user.getPasswordHash());

        verify(userRepository).findByIdForUpdate(holderId);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
    }

    @Test
    void changeRole_shouldChangeRoleSuccessfully() {
        // given
        Long holderId = 1L;
        User user = new User();
        user.setId(holderId);

        Role newRole = Role.ADMIN;

        when(userRepository.findByIdForUpdate(holderId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        User updated = userService.changeRole(holderId, newRole);

        // then
        assertNotNull(updated);
        assertEquals(newRole, user.getRole());

        verify(userRepository).findByIdForUpdate(holderId);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_shouldDeleteUserSuccessfully() {
        // given
        Long id = 1L;

        when(userRepository.existsById(id)).thenReturn(true);

        // when
        userService.deleteUser(id);

        // then
        verify(userRepository).existsById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotExists() {
        // given
        Long id = 1L;

        when(userRepository.existsById(id)).thenReturn(false);

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(id));

        verify(userRepository).existsById(id);
        verify(userRepository, never()).deleteById(any());
    }
}