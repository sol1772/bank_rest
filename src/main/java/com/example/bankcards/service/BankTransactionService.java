package com.example.bankcards.service;

import com.example.bankcards.dto.BankTransactionDto;
import com.example.bankcards.dto.mappers.BankTransactionMapper;
import com.example.bankcards.entity.BankTransaction;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.repository.BankTransactionRepository;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BankTransactionService {
    private final BankTransactionRepository bankTransactionRepository;
    private final BankTransactionMapper mapper;
    private final CardRepository cardRepository;

    @PreAuthorize("hasAuthority('ADMIN')")
    public BankTransaction getById(Long id) {
        return bankTransactionRepository.findById(id)
                .orElseThrow(() -> new AppRuntimeException(
                        String.format("Bank transaction with id %d does not exist", id)));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<BankTransactionDto> getAll(Pageable pageable) {
        return bankTransactionRepository.findAll(pageable).map(mapper::toDto);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<BankTransactionDto> getAllByFromCardId(Long cardId, Pageable pageable) {
        return bankTransactionRepository.findByFromCardId(cardId, pageable).map(mapper::toDto);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<BankTransactionDto> getAllByToCardId(Long cardId, Pageable pageable) {
        return bankTransactionRepository.findByToCardId(cardId, pageable).map(mapper::toDto);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<BankTransactionDto> getByAmount(BigDecimal amount, Pageable pageable) {
        return bankTransactionRepository.findByAmount(amount, pageable).map(mapper::toDto);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<BankTransactionDto> getByStatus(TransactionStatus status, Pageable pageable) {
        return bankTransactionRepository.findByStatus(status, pageable).map(mapper::toDto);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public BankTransaction getByTransactionReference(String transactionReference) {
        return bankTransactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new AppRuntimeException(
                        String.format("Bank transaction with reference %s does not exist", transactionReference)));
    }

    @Transactional
    public void fundTransfer(BankTransaction transaction, Long fromCardId, Long toCardId, BigDecimal amount) {
        try {
            Card fromCard = cardRepository.findByIdForUpdate(fromCardId)
                    .orElseThrow(() -> new AppRuntimeException("Source card not found"));
            Card toCard = cardRepository.findByIdForUpdate(toCardId)
                    .orElseThrow(() -> new AppRuntimeException("Recipient card not found"));

            if (!fromCard.getHolder().equals(toCard.getHolder())) {
                throw new AppRuntimeException("The holders of the source and recipient cards do not match.");
            }
            if (fromCard.getStatus() != CardStatus.ACTIVE) {
                throw new AppRuntimeException("Source card is not active!");
            }
            if (toCard.getStatus() != CardStatus.ACTIVE) {
                throw new AppRuntimeException("Recipient card is not active!");
            }
            if (fromCard.getBalance().compareTo(amount) < 0) {
                throw new AppRuntimeException(
                        String.format("Not enough funds on card %s", fromCardId));
            }

            transaction.setStatus(TransactionStatus.PROCESSING);
            bankTransactionRepository.save(transaction);

            fromCard.setBalance(fromCard.getBalance().subtract(amount));
            cardRepository.save(fromCard);

            toCard.setBalance(toCard.getBalance().add(amount));
            cardRepository.save(toCard);

            transaction.setStatus(TransactionStatus.SUCCESS);
            bankTransactionRepository.save(transaction);
            log.info("Transaction {} successful", transaction.getId());
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            bankTransactionRepository.save(transaction);
            log.error("Transaction {} failed: {}", transaction.getId(), e.getMessage());
            throw e;
        }
    }
}
