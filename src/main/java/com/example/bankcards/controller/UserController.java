package com.example.bankcards.controller;

import com.example.bankcards.dto.PageRequestDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.mappers.UserMapper;
import com.example.bankcards.dto.request.ChangePasswordRequest;
import com.example.bankcards.dto.request.CreateUserRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.service.UserService;
import com.example.bankcards.service.validators.UserValidator;
import com.example.bankcards.util.AppErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static com.example.bankcards.util.ErrorsUtil.returnErrorsToClient;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserValidator userValidator;
    private final UserMapper userMapper;

    // ===================== GET =====================
    @GetMapping
    @Operation(summary = "Get all users (ADMIN)")
    public ResponseEntity<Page<UserDto>> getUsers(@ModelAttribute PageRequestDto pageRequestDto) {
        if (pageRequestDto.getSortBy() == null) {
            pageRequestDto.setSortBy("username");
        }
        Page<UserDto> usersPage = userService.getAll(pageRequestDto.toPageable());
        return ResponseEntity.ok(usersPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID (ADMIN)")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @GetMapping("/api/users/me")
    @Operation(summary = "Get own profile (USER)")
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    // ===================== CREATE =====================
    @PostMapping
    @Operation(summary = "Create user (ADMIN)")
    public ResponseEntity<UserDto> addUser(@Valid @RequestBody CreateUserRequest request, BindingResult bindingResult) {
        User user = userMapper.toEntity(request.getUser());
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            returnErrorsToClient(bindingResult);
        }
        User created = userService.createUser(user, request.getPassword());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(userMapper.toDto(created));
    }

    // ===================== UPDATE =====================
    @PatchMapping("/{id}/role")
    @Operation(summary = "Change role (ADMIN)")
    public ResponseEntity<UserDto> changeRole(@PathVariable Long id, @RequestBody Role newRole) {
        User updated = userService.changeRole(id, newRole);
        return ResponseEntity.ok(userMapper.toDto(updated));
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = "Change password (USER)")
    public ResponseEntity<UserDto> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest request) {
        User updated = userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(userMapper.toDto(updated));
    }

    @PatchMapping("/{id}/reset-password")
    @Operation(summary = "Reset password (ADMIN)")
    public ResponseEntity<UserDto> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        User updated = userService.resetPassword(id, newPassword);
        return ResponseEntity.ok(userMapper.toDto(updated));
    }

    // ===================== DELETE =====================
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user (ADMIN)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ===================== EXCEPTIONS =====================
    @ExceptionHandler
    private ResponseEntity<AppErrorResponse> handleException(AppRuntimeException e) {
        AppErrorResponse response = new AppErrorResponse(e.getMessage(), System.currentTimeMillis());
        return ResponseEntity.badRequest().body(response);
    }
}
