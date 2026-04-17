package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceSecurityTest {
    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private BCryptPasswordEncoder passwordEncoder;

    private User getTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setRole(Role.USER);
        return user;
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createUser_shouldAllow_whenAdmin() {
        // given
        Long id = 1L;
        User user = getTestUser(id);

        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        userService.createUser(user, "newPass");

        // then
        verify(userRepository).save(user);
    }

    @Test
    @WithMockUser(authorities = "USER")
    void createUser_shouldDeny_whenNotAdmin() {
        // given
        Long id = 1L;
        User user = getTestUser(id);

        // when & then
        assertThrows(AccessDeniedException.class, () -> userService.createUser(user, "newPass"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void changePassword_shouldAllow_whenSameUser() {
        // given
        Long id = 1L;

        User user = getTestUser(id);
        user.setPasswordHash("old");

        when(userRepository.findByIdForUpdate(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("new");

        // replace the principal
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new CustomUserDetails(user), null, List.of()
                )
        );

        // when
        userService.changePassword(id, "old", "new");

        // then
        verify(userRepository).save(user);
    }

    @Test
    @WithMockUser
    void changePassword_shouldDeny_whenDifferentUser() {
        // given
        Long id = 1L;

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new CustomUserDetails(getTestUser(999L)), null, List.of()
                )
        );

        // when & then
        assertThrows(AccessDeniedException.class,
                () -> userService.changePassword(id, "old", "new"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteUser_shouldAllow_whenAdmin() {
        // given
        Long id = 1L;
        when(userRepository.existsById(id)).thenReturn(true);

        // when
        userService.deleteUser(id);

        // then
        verify(userRepository).deleteById(id);
    }

    @Test
    @WithMockUser(authorities = "USER")
    void deleteUser_shouldDeny_whenNotAdmin() {
        // given
        Long id = 1L;

        // when & then
        assertThrows(AccessDeniedException.class, () -> userService.deleteUser(id));

        verify(userRepository, never()).deleteById(any());
    }
}
