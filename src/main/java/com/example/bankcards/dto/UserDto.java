package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.Role;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private Role role;
}
