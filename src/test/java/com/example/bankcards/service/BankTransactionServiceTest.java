package com.example.bankcards.service;

import com.example.bankcards.entity.BankTransaction;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.BankTransactionRepository;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankTransactionServiceTest {
    @Mock
    private BankTransactionRepository bankTransactionRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private BankTransactionService transactionService;

    private Card getTestCard(Long id, User user) {
        Card card = new Card();
        card.setId(id);
        card.setHolder(user);
        card.setStatus(CardStatus.ACTIVE);
        return card;
    }

    private User getTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setRole(Role.USER);
        return user;
    }

    @Test
    void fundTransfer_shouldFundTransferSuccessfully() {
        // given
        Long fromCardId = 1L;
        Long toCardId = 2L;
        User user = getTestUser(1L);

        Card fromCard = getTestCard(fromCardId, user);
        fromCard.setBalance(new BigDecimal("100.00"));
        Card toCard = getTestCard(toCardId, user);
        toCard.setBalance(new BigDecimal("50.00"));

        String transactionId = UUID.randomUUID().toString();
        BigDecimal amount = new BigDecimal("10.00");

        BankTransaction transaction = new BankTransaction(transactionId, fromCard, toCard, amount, TransactionStatus.PENDING);

        when(cardRepository.findByIdForUpdate(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(toCardId)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bankTransactionRepository.save(any(BankTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        transactionService.fundTransfer(transaction, fromCardId, toCardId, amount);

        // then
        assertEquals(new BigDecimal("90.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("60.00"), toCard.getBalance());
        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());

        verify(cardRepository, times(2)).save(fromCard);
        verify(cardRepository, times(2)).save(toCard);
        verify(bankTransactionRepository, times(2)).save(transaction);
    }

    @Test
    void fundTransfer_shouldThrowException_whenNotEnoughFunds() {
        // given
        Long fromId = 1L;
        Long toId = 2L;

        User user = getTestUser(1L);

        Card fromCard = getTestCard(fromId, user);
        fromCard.setBalance(new BigDecimal("5.00")); // not enough
        fromCard.setStatus(CardStatus.ACTIVE);

        Card toCard = getTestCard(toId, user);
        toCard.setBalance(new BigDecimal("50.00"));
        toCard.setStatus(CardStatus.ACTIVE);

        BigDecimal amount = new BigDecimal("10.00");

        BankTransaction transaction = new BankTransaction();

        when(cardRepository.findByIdForUpdate(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(toId)).thenReturn(Optional.of(toCard));

        // when & then
        assertThrows(AppRuntimeException.class,
                () -> transactionService.fundTransfer(transaction, fromId, toId, amount));

        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void fundTransfer_shouldThrowException_whenFromCardNotActive() {
        // given
        Long fromId = 1L;
        Long toId = 2L;

        User user = getTestUser(1L);

        Card fromCard = getTestCard(fromId, user);
        fromCard.setStatus(CardStatus.BLOCKED); // !

        Card toCard = getTestCard(toId, user);
        toCard.setStatus(CardStatus.ACTIVE);

        BigDecimal amount = new BigDecimal("10.00");

        BankTransaction transaction = new BankTransaction();

        when(cardRepository.findByIdForUpdate(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(toId)).thenReturn(Optional.of(toCard));

        // when & then
        assertThrows(AppRuntimeException.class,
                () -> transactionService.fundTransfer(transaction, fromId, toId, amount));

        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void fundTransfer_shouldThrowException_whenDifferentHolders() {
        // given
        Long fromId = 1L;
        Long toId = 2L;

        User user1 = getTestUser(1L);
        user1.setUsername("User1");
        User user2 = getTestUser(2L);
        user2.setUsername("User2");

        Card fromCard = getTestCard(fromId, user1);
        fromCard.setBalance(new BigDecimal("100.00"));

        Card toCard = getTestCard(toId, user2); // another holder !
        toCard.setBalance(new BigDecimal("50.00"));

        String transactionId = UUID.randomUUID().toString();
        BigDecimal amount = new BigDecimal("10.00");

        BankTransaction transaction = new BankTransaction(transactionId, fromCard, toCard, amount, TransactionStatus.PENDING);

        when(cardRepository.findByIdForUpdate(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdForUpdate(toId)).thenReturn(Optional.of(toCard));

        // when & then
        assertThrows(AppRuntimeException.class,
                () -> transactionService.fundTransfer(transaction, fromId, toId, amount));

        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void fundTransfer_shouldThrowException_whenFromCardNotFound() {
        // given
        Long fromId = 1L;
        Long toId = 2L;

        when(cardRepository.findByIdForUpdate(fromId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.fundTransfer(new BankTransaction(), fromId, toId, BigDecimal.TEN));

        verify(cardRepository, never()).save(any());
    }
}