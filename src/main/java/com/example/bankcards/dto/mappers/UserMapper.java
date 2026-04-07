package com.example.bankcards.dto.mappers;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDto toDto(User user);

    User toEntity(UserDto userDto);
}
