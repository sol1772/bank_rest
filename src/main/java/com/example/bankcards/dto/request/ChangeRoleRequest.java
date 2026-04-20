package com.example.bankcards.dto.request;

import com.example.bankcards.entity.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleRequest {
    @NotNull(message = "Role must not be null")
    @Enumerated(EnumType.STRING)
    private Role role;
}
