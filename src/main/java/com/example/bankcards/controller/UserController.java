package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.mappers.UserMapper;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.service.UserService;
import com.example.bankcards.service.validators.UserValidator;
import com.example.bankcards.util.AppErrorResponse;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.util.ErrorsUtil.returnErrorsToClient;
import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserValidator userValidator;
    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers() {
        List<UserDto> users = userService.getAll()
                .stream()
                .map(mapper::toDto)
                .collect(toList());
        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.of(Optional.ofNullable(mapper.toDto(user)));
    }

    @PostMapping("/add")
    public ResponseEntity<UserDto> addUser(@RequestBody UserDto userDto, String password, BindingResult bindingResult) {
        User user = mapper.toEntity(userDto);
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            returnErrorsToClient(bindingResult);
        }
        return getNewUser(user, password);
    }

    @PutMapping
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto) {
        User updated = userService.updateUser(mapper.toEntity(userDto));
        return ResponseEntity.of(Optional.ofNullable(mapper.toDto(updated)));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<UserDto> changePassword(@PathVariable("id") Long id, String oldPassword, String newPassword) {
        User updated = userService.changePassword(id, oldPassword, newPassword);
        return ResponseEntity.of(Optional.ofNullable(mapper.toDto(updated)));
    }

    public ResponseEntity<UserDto> getNewUser(@RequestBody User user, String password) {
        User created = userService.createUser(user, password);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    @ExceptionHandler
    private ResponseEntity<AppErrorResponse> handleException(AppRuntimeException e) {
        AppErrorResponse response = new AppErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
