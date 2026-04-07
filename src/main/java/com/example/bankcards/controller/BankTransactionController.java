package com.example.bankcards.controller;

import com.example.bankcards.dto.BankTransactionDto;
import com.example.bankcards.dto.mappers.BankTransactionMapper;
import com.example.bankcards.entity.BankTransaction;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.service.BankTransactionService;
import com.example.bankcards.service.validators.BankTransactionValidator;
import com.example.bankcards.util.AppErrorResponse;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.bankcards.util.ErrorsUtil.returnErrorsToClient;
import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class BankTransactionController {
    private final BankTransactionService bankTransactionService;
    private final BankTransactionValidator bankTransactionValidator;
    private final BankTransactionMapper mapper = Mappers.getMapper(BankTransactionMapper.class);
    ;

    @GetMapping
    public ResponseEntity<List<BankTransactionDto>> getBankTransactions() {
        List<BankTransactionDto> transactions = bankTransactionService.getAll()
                .stream()
                .map(mapper::toDto)
                .collect(toList());
        return ResponseEntity.ok().body(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankTransactionDto> getBankTransaction(@PathVariable("id") Long id) {
        BankTransaction transaction = bankTransactionService.getById(id);
        return ResponseEntity.of(Optional.ofNullable(mapper.toDto(transaction)));
    }

    @PostMapping("/transfer")
    public ResponseEntity<BankTransactionDto> transferFunds(Long fromCardId, Long toCardId,
                                                            BigDecimal amount, BindingResult bindingResult) {
        String transactionId = UUID.randomUUID().toString();
        BankTransaction transaction = new BankTransaction(transactionId, fromCardId, toCardId, amount, TransactionStatus.PENDING);
        bankTransactionValidator.validate(transaction, bindingResult);
        if (bindingResult.hasErrors()) {
            returnErrorsToClient(bindingResult);
        }
        bankTransactionService.fundTransfer(transaction, fromCardId, toCardId, amount);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler
    private ResponseEntity<AppErrorResponse> handleException(AppRuntimeException e) {
        AppErrorResponse response = new AppErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
