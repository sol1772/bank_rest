package com.example.bankcards.service;

import com.example.bankcards.dto.BankTransactionDto;
import com.example.bankcards.entity.BankTransaction;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Service for managing bank transactions.
 * <p>
 * Provides operations for retrieving and performing transactions.
 */
public interface BankTransactionService {
    /**
     * Returns transaction by id (ADMIN only).
     *
     * @param id transaction id
     * @return transaction entity
     * @throws ResourceNotFoundException if transaction does not exist
     */
    BankTransaction getById(Long id);

    /**
     * Returns paginated list of all transactions (ADMIN only).
     *
     * @param pageable pagination info
     * @return page of transactions
     */
    Page<BankTransactionDto> getAll(Pageable pageable);

    /**
     * Returns transactions where the given card is sender.
     *
     * @param cardId   sender card id
     * @param pageable pagination info
     * @return page of transactions
     */
    Page<BankTransactionDto> getAllBySenderCardId(Long cardId, Pageable pageable);

    /**
     * Returns transactions where the given card is receiver.
     *
     * @param cardId   receiver card id
     * @param pageable pagination info
     * @return page of transactions
     */
    Page<BankTransactionDto> getAllByReceiverCardId(Long cardId, Pageable pageable);

    /**
     * Returns transactions filtered by amount (ADMIN only).
     *
     * @param amount   transaction amount
     * @param pageable pagination info
     * @return page of transactions
     */
    Page<BankTransactionDto> getByAmount(BigDecimal amount, Pageable pageable);

    /**
     * Returns transactions filtered by status (ADMIN only).
     *
     * @param status   transaction status
     * @param pageable pagination info
     * @return page of transactions
     */
    Page<BankTransactionDto> getByStatus(TransactionStatus status, Pageable pageable);

    /**
     * Returns transaction by reference.
     *
     * @param transactionReference unique reference
     * @return transaction entity
     * @throws ResourceNotFoundException if transaction not found
     */
    BankTransaction getByTransactionReference(String transactionReference);

    /**
     * Performs fund transfer between two cards.
     *
     * @param transaction transaction entity
     * @param fromCardId  sender card id
     * @param toCardId    receiver card id
     * @param amount      transfer amount
     * @throws ResourceNotFoundException if any card does not exist
     * @throws IllegalArgumentException  if amount is invalid
     */
    void transferFunds(BankTransaction transaction, Long fromCardId, Long toCardId, BigDecimal amount);
}
