package com.example.bankcards.repository;

import com.example.bankcards.entity.BankTransaction;
import com.example.bankcards.entity.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    Page<BankTransaction> findByFromCardId(Long fromCardId, Pageable pageable);

    Page<BankTransaction> findByToCardId(Long toCardId, Pageable pageable);

    Page<BankTransaction> findByAmount(BigDecimal amount, Pageable pageable);

    Page<BankTransaction> findByStatus(TransactionStatus status, Pageable pageable);

    Optional<BankTransaction> findByTransactionReference(String transactionReference);
}
