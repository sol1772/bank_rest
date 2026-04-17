package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockingRequest;
import com.example.bankcards.entity.enums.CardBlockingRequestStatus;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardBlockingRequestRepository;
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
public class CardBlockingRequestService {
    private final CardBlockingRequestRepository cardBlockingRequestRepository;
    private final CardService cardService;
    private final CardRepository cardRepository;

    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<CardBlockingRequest> getAll(Pageable pageable) {
        return cardBlockingRequestRepository.findAll(pageable);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public CardBlockingRequest getById(Long id) {
        return cardBlockingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Card blocking request with id %d does not exist", id)));
    }

    @Transactional
    public void createCardBlockRequest(Long cardId, String username) {
        Card card = cardService.getCardByIdAndUser(cardId, username);
        CardBlockingRequest blockingRequest = new CardBlockingRequest(card, CardBlockingRequestStatus.PROCESSING);
        cardBlockingRequestRepository.save(blockingRequest);
        if (card.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            blockingRequest.setRequestStatus(CardBlockingRequestStatus.GRANTED);
            cardBlockingRequestRepository.save(blockingRequest);

            card.setStatus(CardStatus.BLOCKED);
            cardRepository.save(card);
            log.info("The card is blocked: {}. ", card.getId());
        } else {
            blockingRequest.setRequestStatus(CardBlockingRequestStatus.REJECTED);
            cardBlockingRequestRepository.save(blockingRequest);
            log.info("Attempt to block the card with a non-zero balance: {}.", card.getId());
            throw new AppRuntimeException(String.format("Card %s has a non-zero balance.", card.getId()));
        }
    }
}
