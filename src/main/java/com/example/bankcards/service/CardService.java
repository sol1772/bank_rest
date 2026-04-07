package com.example.bankcards.service;

import com.example.bankcards.dto.PageRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final CardRepository cardRepository;
    private final PageRequestDto pageRequestDto = new PageRequestDto();

    @PreAuthorize("hasAuthority('ADMIN')")
    public Card getCardById(Long id) {
        Optional<Card> card = cardRepository.findById(id);
        if (card.isPresent()) {
            return card.get();
        } else
            throw new AppRuntimeException(String.format("Card with id %d does not exist", id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<Card> getAll() {
        pageRequestDto.setSortByColumn("number");
        return cardRepository.findAll(pageRequestDto.getPageable());
    }

    public Page<Card> getAllByUser(String username) {
        pageRequestDto.setSortByColumn("number");
        return cardRepository.findAllByHolder_Username(username, pageRequestDto.getPageable());
    }

    public BigDecimal getCardBalance(Long id, String username) {
        Card card = cardRepository.findByIdAndHolder_Username(id, username);
        if (card != null) {
            return card.getBalance();
        } else {
            throw new AppRuntimeException(String.format("Card with id %d does not exist", id));
        }
    }

    public BigDecimal getUserBalance(String username) {
        return cardRepository.getUserBalance(username);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public Card createCard(Card card) {
        assert card != null;
        if (card.getId() != null) {
            card.setId(null);
        }
        card.setCvc();
        Card created = cardRepository.save(card);
        if (logger.isInfoEnabled()) {
            logger.info("Card {} is created", created);
        }
        return created;
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public void activateCard(Long id) {
        Optional<Card> card = cardRepository.findById(id);
        if (card.isPresent()) {
            if (card.get().getStatus() != CardStatus.ACTIVE) {
                card.get().setStatus(CardStatus.ACTIVE);
                Card updated = cardRepository.save(card.get());
                if (logger.isInfoEnabled()) {
                    logger.info("Card {} is activated", updated);
                }
            } else {
                throw new AppRuntimeException("Card is already active!");
            }
        } else {
            throw new AppRuntimeException(String.format("Card with id %d does not exist", id));
        }
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public void blockCard(Long id) {
        Optional<Card> card = cardRepository.findById(id);
        if (card.isPresent()) {
            if (card.get().getStatus() != CardStatus.BLOCKED) {
                card.get().setStatus(CardStatus.BLOCKED);
                Card updated = cardRepository.save(card.get());
                if (logger.isInfoEnabled()) {
                    logger.info("Card {} is blocked", updated);
                }
            } else {
                throw new AppRuntimeException("Card is already blocked!");
            }
        } else {
            throw new AppRuntimeException(String.format("Card with id %d does not exist", id));
        }
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new AppRuntimeException(String.format("Card with id %d does not exist", id));
        }
        cardRepository.deleteById(id);
        if (logger.isInfoEnabled()) {
            logger.info("Card with id {} is deleted", id);
        }
    }
}
