package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.mappers.CardMapper;
import com.example.bankcards.dto.mappers.UserMapper;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CryptoUtil;
import com.example.bankcards.util.MaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CardService {
    private static final SecureRandom RANDOMIZER = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final UserMapper userMapper;
    private final CryptoUtil cryptoUtil;

    @PreAuthorize("hasAuthority('ADMIN')")
    public CardDto getCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new AppRuntimeException(
                        String.format("Card with id %d does not exist", id)));
        return toDtoDecryptedMasked(card);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<CardDto> getAll(Pageable pageable) {
        Page<Card> cardsPage = cardRepository.findAll(pageable);
        cardsPage.forEach(this::toDtoDecryptedMasked);
        return cardsPage.map(cardMapper::toDto);
    }

    public Card getCardByIdAndUser(Long id, String username) {
        return cardRepository.findByIdAndHolder_Username(id, username)
                .orElseThrow(() -> new AppRuntimeException(
                        String.format("Card with id %d and holder %s does not exist", id, username)));
    }

    public Page<CardDto> getAllByUser(Pageable pageable, String username) {
        Page<Card> cardsPage = cardRepository.findAllByHolder_Username(username, pageable);
        cardsPage.forEach(this::toDtoDecrypted);
        return cardsPage.map(cardMapper::toDto);
    }

    public BigDecimal getCardBalance(Long id, String username) {
        Card card = cardRepository.findByIdAndHolder_Username(id, username)
                .orElseThrow(() -> new AppRuntimeException(
                        String.format("Card with id %d and holder %s does not exist", id, username)));
        return card.getBalance();
    }

    public BigDecimal getUserBalance(String username) {
        return cardRepository.getUserBalance(username);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public Card createCard(Card card, Long holderId) {
        Objects.requireNonNull(card, "Card must not be null");
        if (card.getId() != null) {
            card.setId(null);
        }
        User holder = userRepository.findById(holderId)
                .orElseThrow(() -> new AppRuntimeException(String.format("User with id %d does not exist", holderId)));
        card.setHolder(holder);
        card.setStatus(CardStatus.ACTIVE);

        encryptCard(card);

        Card created = cardRepository.save(card);
        log.info("Card {} is created", created.getId());
        return created;
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public void activateCard(Long id) {
        Card card = cardRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppRuntimeException(
                        String.format("Card with id %d does not exist", id)));
        if (card.getStatus() != CardStatus.ACTIVE) {
            card.setStatus(CardStatus.ACTIVE);
            Card updated = cardRepository.save(card);
            log.info("Card {} is activated", updated.getId());
        } else {
            throw new AppRuntimeException("Card is already active!");
        }
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public void blockCard(Long id) {
        Card card = cardRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppRuntimeException(
                        String.format("Card with id %d does not exist", id)));
        if (card.getStatus() != CardStatus.BLOCKED) {
            card.setStatus(CardStatus.BLOCKED);
            Card updated = cardRepository.save(card);
            log.info("Card {} is blocked", updated.getId());
        } else {
            throw new AppRuntimeException("Card is already blocked!");
        }
    }

    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new AppRuntimeException(String.format("Card with id %d does not exist", id));
        }
        cardRepository.deleteById(id);
        log.info("Card with id {} is deleted", id);
    }

    private void encryptCard(Card card) {
        byte[] iv = CryptoUtil.getInitializationVector();
        card.setIv(ENCODER.encodeToString(iv));

        card.setNumber(cryptoUtil.encrypt(card.getNumber(), iv));

        String cvc = String.valueOf((int) ((RANDOMIZER.nextDouble() * 899) + 100));
        card.setCvc(cryptoUtil.encrypt(cvc, iv));
    }

    private byte[] getIv(Card card) {
        return DECODER.decode(card.getIv());
    }

    private CardDto toDtoDecrypted(Card card) {
        String decrypted = cryptoUtil.decrypt(card.getNumber(), getIv(card));

        return CardDto.builder()
                .id(card.getId())
                .number(decrypted)
                .holder(userMapper.toDto(card.getHolder()))
                .expiry(card.getExpiry())
                .status(card.getStatus())
                .balance(card.getBalance())
                .build();
    }

    private CardDto toDtoDecryptedMasked(Card card) {
        String decrypted = cryptoUtil.decrypt(card.getNumber(), getIv(card));
        String masked = MaskingUtil.maskCardNumber(decrypted);

        return CardDto.builder()
                .id(card.getId())
                .number(masked)
                .holder(userMapper.toDto(card.getHolder()))
                .expiry(card.getExpiry())
                .status(card.getStatus())
                .balance(card.getBalance())
                .build();
    }
}
