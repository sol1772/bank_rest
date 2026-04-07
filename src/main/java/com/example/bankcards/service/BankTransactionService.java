package com.example.bankcards.service;

import com.example.bankcards.dto.PageRequestDto;
import com.example.bankcards.entity.BankTransaction;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.repository.BankTransactionRepository;
import com.example.bankcards.repository.CardRepository;
import jakarta.persistence.Transient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankTransactionService {
    private static final Logger logger = LoggerFactory.getLogger(BankTransactionService.class);
    private final BankTransactionRepository bankTransactionRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;
    private final PageRequestDto pageRequestDto = new PageRequestDto();
    @Transient
    private final Lock balanceChangeLock = new ReentrantLock();

    @PreAuthorize("hasRole('ADMIN')")
    public BankTransaction getById(Long id) {
        Optional<BankTransaction> bankTransaction = bankTransactionRepository.findById(id);
        if (bankTransaction.isPresent()) {
            return bankTransaction.get();
        } else
            throw new AppRuntimeException(String.format("Bank transaction with id %d does not exist", id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<BankTransaction> getAll() {
        pageRequestDto.setSortByColumn("createdAt");
        Pageable pageable = pageRequestDto.getPageable();
        return bankTransactionRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<BankTransaction> getAllByFromCardId(Long cardId) {
        pageRequestDto.setSortByColumn("createdAt");
        return bankTransactionRepository.findByFromCardId(cardId, pageRequestDto.getPageable());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<BankTransaction> getAllByToCardId(Long cardId) {
        pageRequestDto.setSortByColumn("createdAt");
        return bankTransactionRepository.findByToCardId(cardId, pageRequestDto.getPageable());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public BankTransaction getByTransactionReference(String transactionReference) {
        Optional<BankTransaction> bankTransaction = bankTransactionRepository.findByTransactionReference(transactionReference);
        if (bankTransaction.isPresent()) {
            return bankTransaction.get();
        } else
            throw new AppRuntimeException(String.format("Bank transaction with reference %s does not exist", transactionReference));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<BankTransaction> getByAmount(BigDecimal amount) {
        pageRequestDto.setSortByColumn("createdAt");
        return bankTransactionRepository.findByAmount(amount, pageRequestDto.getPageable());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<BankTransaction> getByStatus(CardStatus status) {
        pageRequestDto.setSortByColumn("createdAt");
        return bankTransactionRepository.findByStatus(status, pageRequestDto.getPageable());
    }

    @Transactional
    public void fundTransfer(BankTransaction transaction, Long fromCardId, Long toCardId, BigDecimal amount) {
        Card fromCard = cardService.getCardById(fromCardId);
        Card toCard = cardService.getCardById(toCardId);
        if (!fromCard.getHolder().equals(toCard.getHolder())) {
            throw new AppRuntimeException("The holders of the source card and the recipient card do not match.");
        }

        transaction.setStatus(TransactionStatus.PROCESSING);
        bankTransactionRepository.save(transaction);
        try {
            balanceChangeLock.lock();
            BigDecimal fromCardBalance = fromCard.getBalance();
            if (fromCardBalance.compareTo(amount) >= 0) {
                // source card
                fromCardBalance = fromCard.getBalance().subtract(amount);
                fromCard.setBalance(fromCardBalance);
                cardRepository.save(fromCard);
                logger.info("{} card balance reduced by: {} ", fromCard, amount);

                // destination card
                BigDecimal toCardBalance = toCard.getBalance().add(amount);
                toCard.setBalance(toCardBalance);
                cardRepository.save(toCard);
                logger.info("{} card balance increased by: {} ", toCard, amount);

                //bank transaction
                transaction.setStatus(TransactionStatus.SUCCESS);
                bankTransactionRepository.save(transaction);
                logger.info("Transaction successful {}", transaction);
            } else {
                logger.info("Attempt to withdraw funds from the card: {}. Not enough funds to debit the amount of {}." +
                        " Withdrawal cancelled.", fromCard, amount);
                throw new AppRuntimeException(String.format("Not enough funds to debit the card %s", fromCard));
            }
        } finally {
            balanceChangeLock.unlock();
            transaction.setStatus(TransactionStatus.FAILED);
            bankTransactionRepository.save(transaction);
            if (logger.isErrorEnabled()) {
                logger.info("Transaction error {}", transaction);
            }
        }
    }
}
