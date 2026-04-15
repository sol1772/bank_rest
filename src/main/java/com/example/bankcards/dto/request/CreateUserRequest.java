package com.example.bankcards.dto.request;

import com.example.bankcards.dto.UserDto;
import lombok.Data;

@Data
public class CreateUserRequest {
    private UserDto user;
    private String password;
}
