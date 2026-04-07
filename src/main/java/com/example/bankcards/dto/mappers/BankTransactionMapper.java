package com.example.bankcards.dto.mappers;

import com.example.bankcards.dto.BankTransactionDto;
import com.example.bankcards.entity.BankTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BankTransactionMapper {
    BankTransactionDto toDto(BankTransaction bankTransaction);

    BankTransaction toEntity(BankTransactionDto bankTransactionDto);
}
