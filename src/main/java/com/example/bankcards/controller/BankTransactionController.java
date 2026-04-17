package com.example.bankcards.controller;

import com.example.bankcards.dto.BankTransactionDto;
import com.example.bankcards.dto.PageRequestDto;
import com.example.bankcards.dto.mappers.BankTransactionMapper;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.entity.BankTransaction;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.service.BankTransactionService;
import com.example.bankcards.service.validators.BankTransactionValidator;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

import static com.example.bankcards.util.ErrorsUtil.returnErrorsToClient;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class BankTransactionController {
    private final BankTransactionService bankTransactionService;
    private final BankTransactionValidator bankTransactionValidator;
    private final BankTransactionMapper mapper;

    // ===================== GET =====================
    @GetMapping
    @Operation(summary = "Get all transactions (ADMIN)")
    public ResponseEntity<Page<BankTransactionDto>> getBankTransactions(@ModelAttribute PageRequestDto pageRequestDto) {
        if (pageRequestDto.getSortBy() == null) {
            pageRequestDto.setSortBy("createdAt");
        }
        Page<BankTransactionDto> transactions = bankTransactionService.getAll(pageRequestDto.toPageable());
        return ResponseEntity.ok().body(transactions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID (ADMIN)")
    public ResponseEntity<BankTransactionDto> getBankTransaction(@PathVariable Long id) {
        BankTransaction transaction = bankTransactionService.getById(id);
        return ResponseEntity.ok(mapper.toDto(transaction));
    }

    // ===================== CREATE =====================
    @PostMapping("/transfer")
    @Operation(summary = "Create transaction (USER)")
    public ResponseEntity<BankTransactionDto> transferFunds(@RequestBody TransferRequest request,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails,
                                                            BindingResult bindingResult) {
        Card fromCard = request.getFromCard();
        Card toCard = request.getToCard();
        BigDecimal amount = request.getAmount();

        if (!fromCard.getHolder().getUsername().equals(userDetails.getUsername())) {
            throw new AppRuntimeException("User does not match source card holder!");
        }

        String transactionId = UUID.randomUUID().toString();
        BankTransaction transaction = new BankTransaction(transactionId, fromCard, toCard, amount, TransactionStatus.PENDING);
        bankTransactionValidator.validate(transaction, bindingResult);
        if (bindingResult.hasErrors()) {
            returnErrorsToClient(bindingResult);
        }

        bankTransactionService.fundTransfer(transaction, fromCard.getId(), toCard.getId(), amount);
        return ResponseEntity.noContent().build();
    }
}
