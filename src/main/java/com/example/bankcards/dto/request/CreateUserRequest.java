package com.example.bankcards.dto.request;

import com.example.bankcards.dto.UserDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    private UserDto user;

    @NotBlank(message = "Password is required")
    private String password;
}
