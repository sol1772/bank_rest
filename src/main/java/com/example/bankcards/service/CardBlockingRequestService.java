package com.example.bankcards.service;

import com.example.bankcards.dto.PageRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockingRequest;
import com.example.bankcards.entity.enums.CardBlockingRequestStatus;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.repository.CardBlockingRequestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardBlockingRequestService {
    private static final Logger logger = LoggerFactory.getLogger(CardBlockingRequestService.class);
    private final CardBlockingRequestRepository cardBlockingRequestRepository;
    private final CardService cardService;
    private final PageRequestDto pageRequestDto = new PageRequestDto();

    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardBlockingRequest> getAll() {
        pageRequestDto.setSortByColumn("createdAt");
        Pageable pageable = pageRequestDto.getPageable();
        return cardBlockingRequestRepository.findAll(pageable);
    }

    public void createCardBlockRequest(Long cardId, String username) {
        Card card = cardService.getCardById(cardId);
        if (!card.getHolder().getUsername().equals(username)) {
            logger.info("Attempt to block the card {} by another user: {}.", card, username);
            throw new AppRuntimeException(String.format("User %s is not the holder of the card %s.", username, card));
        }

        CardBlockingRequest blockingRequest = new CardBlockingRequest(cardId, CardBlockingRequestStatus.PROCESSING);
        cardBlockingRequestRepository.save(blockingRequest);
        if (card.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            blockingRequest.setRequestStatus(CardBlockingRequestStatus.GRANTED);
            cardBlockingRequestRepository.save(blockingRequest);
            logger.info("The card is blocked: {}. ", card);
        } else {
            blockingRequest.setRequestStatus(CardBlockingRequestStatus.REJECTED);
            cardBlockingRequestRepository.save(blockingRequest);
            logger.info("Attempt to block the card with a non-zero balance: {}.", card);
            throw new AppRuntimeException(String.format("Card %s has a non-zero balance.", card));
        }
    }
}
