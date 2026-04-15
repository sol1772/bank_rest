package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CryptoUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CryptoUtil cryptoUtil;

    @InjectMocks
    private CardService cardService;

    @Test
    void createCard_shouldCreateCardSuccessfully() {
        // given
        Long holderId = 1L;

        User user = new User();
        user.setId(holderId);

        Card card = new Card();
        card.setNumber("1234567812345678");
        card.setBalance(BigDecimal.TEN);

        when(userRepository.findById(holderId)).thenReturn(Optional.of(user));
        when(cryptoUtil.encrypt(anyString(), any())).thenReturn("encrypted");
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Card result = cardService.createCard(card, holderId);

        // then
        assertNotNull(result);
        assertEquals(user, result.getHolder());
        assertEquals("encrypted", result.getNumber());
        assertNotNull(result.getIv());
        assertNotNull(result.getCvc());

        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_shouldThrowException_whenUserNotFound() {
        // given
        Long holderId = 1L;
        Card card = new Card();

        when(userRepository.findById(holderId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(AppRuntimeException.class, () -> cardService.createCard(card, holderId));

        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCard_shouldGenerateEncryptedCvc() {
        // given
        Long holderId = 1L;

        User user = new User();
        user.setId(holderId);

        Card card = new Card();
        card.setNumber("1234567812345678");

        when(userRepository.findById(holderId)).thenReturn(Optional.of(user));
        when(cryptoUtil.encrypt(anyString(), any())).thenReturn("enc");

        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Card result = cardService.createCard(card, holderId);

        // then
        assertEquals("enc", result.getCvc());
    }

    @Test
    void activateCard_shouldActivateCard() {
        // given
        Long id = 1L;

        Card card = new Card();
        card.setId(id);
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findByIdForUpdate(id)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        cardService.activateCard(id);

        // then
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void activateCard_shouldThrowException_whenAlreadyActive() {
        // given
        Long id = 1L;

        Card card = new Card();
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findByIdForUpdate(id)).thenReturn(Optional.of(card));

        // when & then
        assertThrows(RuntimeException.class, () -> cardService.activateCard(id));
    }

    @Test
    void blockCard_shouldBlockCard() {
        // given
        Long id = 1L;

        Card card = new Card();
        card.setId(id);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findByIdForUpdate(id)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        cardService.blockCard(id);

        // then
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void deleteCard_shouldDeleteCardSuccessfully() {
        // given
        Long id = 1L;

        when(cardRepository.existsById(id)).thenReturn(true);

        // when
        cardService.deleteCard(id);

        // then
        verify(cardRepository).existsById(id);
        verify(cardRepository).deleteById(id);
    }
}