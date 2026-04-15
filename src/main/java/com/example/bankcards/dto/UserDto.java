package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private Role role;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;
}
