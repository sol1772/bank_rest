package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockingRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardBlockingRequestStatus;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.repository.CardBlockingRequestRepository;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardBlockingRequestServiceTest {
    @Mock
    private CardBlockingRequestRepository cardBlockingRequestRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardBlockingRequestService cardBlockingRequestService;

    @Test
    void createCardBlockRequest_shouldCreateSuccessfully() {
        // given
        Long id = 1L;

        User user = new User();
        user.setId(id);
        user.setUsername("User1");

        Card card = new Card();
        card.setId(id);
        card.setHolder(user);
        card.setNumber("1234567812345678");
        card.setBalance(BigDecimal.ZERO);
        card.setStatus(CardStatus.ACTIVE);

        when(cardService.getCardByIdAndUser(id, user.getUsername())).thenReturn(card);

        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        when(cardBlockingRequestRepository.save(any(CardBlockingRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        cardBlockingRequestService.createCardBlockRequest(id, user.getUsername());

        // then
        assertEquals(CardStatus.BLOCKED, card.getStatus());

        verify(cardBlockingRequestRepository, times(2)).save(any());
        verify(cardRepository).save(card);
    }

    @Test
    void createCardBlockRequest_shouldReject_whenBalanceNotZero() {
        // given
        Long id = 1L;
        String username = "User1";

        User user = new User();
        user.setId(id);
        user.setUsername(username);

        Card card = new Card();
        card.setId(id);
        card.setHolder(user);
        card.setBalance(new BigDecimal("100.00")); // not 0
        card.setStatus(CardStatus.ACTIVE);

        when(cardService.getCardByIdAndUser(id, username))
                .thenReturn(card);

        when(cardBlockingRequestRepository.save(any(CardBlockingRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        AppRuntimeException ex = assertThrows(AppRuntimeException.class,
                () -> cardBlockingRequestService.createCardBlockRequest(id, username));

        // then
        assertTrue(ex.getMessage().contains("non-zero balance"));

        verify(cardBlockingRequestRepository, times(2)).save(any());
        verify(cardRepository, never()).save(any()); // card is not blocked
    }

    @Test
    void createCardBlockRequest_shouldThrow_whenCardNotFound() {
        // given
        Long id = 1L;
        String username = "User1";

        when(cardService.getCardByIdAndUser(id, username))
                .thenThrow(new AppRuntimeException("Card not found"));

        // when & then
        assertThrows(AppRuntimeException.class,
                () -> cardBlockingRequestService.createCardBlockRequest(id, username));

        verify(cardBlockingRequestRepository, never()).save(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCardBlockRequest_shouldSetCorrectStatuses() {
        // given
        Long id = 1L;
        String username = "User1";

        Card card = new Card();
        card.setId(id);
        card.setBalance(BigDecimal.ZERO);
        card.setStatus(CardStatus.ACTIVE);

        when(cardService.getCardByIdAndUser(id, username))
                .thenReturn(card);

        List<CardBlockingRequestStatus> statuses = new ArrayList<>();

        when(cardBlockingRequestRepository.save(any()))
                .thenAnswer(inv -> {
                    CardBlockingRequest req = inv.getArgument(0);
                    statuses.add(req.getRequestStatus()); // the status at the moment of the call
                    return req;
                });

        // when
        cardBlockingRequestService.createCardBlockRequest(id, username);

        // then
        assertEquals(2, statuses.size());
        assertEquals(CardBlockingRequestStatus.PROCESSING, statuses.get(0));
        assertEquals(CardBlockingRequestStatus.GRANTED, statuses.get(1));
    }
}