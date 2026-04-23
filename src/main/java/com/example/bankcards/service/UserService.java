package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

/**
 * Service for managing users and their credentials.
 * Provides operations restricted by role-based security.
 * ADMIN users manage accounts, while regular users can access only their own data.
 */
public interface UserService {
    /**
     * Returns user by id.
     * Only ADMIN is allowed to perform this operation.
     *
     * @param id user id
     * @return user entity
     * @throws ResourceNotFoundException if user with id does not exist
     */
    User getUserById(Long id);

    /**
     * Returns paginated list of users (ADMIN only).
     *
     * @param pageable pagination info
     * @return page of users
     */
    Page<UserDto> getAll(Pageable pageable);

    /**
     * Returns user profile.
     * Only the user himself is allowed to perform this operation.
     *
     * @param username username
     * @return user DTO
     * @throws ResourceNotFoundException if user does not exist
     */
    UserDto getProfile(String username);

    /**
     * Creates a new user with the given password (ADMIN only).
     *
     * @param user        user entity (id will be ignored if present)
     * @param newPassword raw password to be encoded
     * @return persisted user
     * @throws IllegalArgumentException if password is empty
     */
    User createUser(User user, String newPassword);

    /**
     * Deletes the user by id (ADMIN only).
     *
     * @param id user id
     * @throws ResourceNotFoundException if user does not exist
     */
    void deleteUser(Long id);

    /**
     * Changes role of the user (ADMIN only).
     *
     * @param id      user id
     * @param newRole new role
     * @return updated user
     * @throws ResourceNotFoundException if user does not exist
     */
    User changeRole(Long id, Role newRole);

    /**
     * Changes password of the user (ADMIN only).
     *
     * @param id          user id
     * @param oldPassword current password (for validation)
     * @param newPassword new password
     * @return updated user
     * @throws AccessDeniedException    if user tries to change another user's password
     * @throws IllegalArgumentException if new password is empty
     */
    User changePassword(Long id, String oldPassword, String newPassword);

    /**
     * Resets password of the user (ADMIN only).
     *
     * @param id          user id
     * @param newPassword new password
     * @return updated user
     * @throws ResourceNotFoundException if user does not exist
     */
    User resetPassword(Long id, String newPassword);
}