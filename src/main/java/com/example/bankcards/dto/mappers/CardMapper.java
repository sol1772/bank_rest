package com.example.bankcards.dto.mappers;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CardMapper {
    CardDto toDto(Card card);

    Card toEntity(CardDto cardDto);

    @Mapping(target = "holder", ignore = true)
    @Mapping(target = "id", ignore = true)
    Card requestToEntity(CreateCardRequest request);
}
