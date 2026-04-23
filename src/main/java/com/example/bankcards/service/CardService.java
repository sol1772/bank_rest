package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Service for managing bank cards and related operations.
 * <p>
 * Most operations are restricted by role-based access control:
 * ADMIN users manage cards, while regular users can access only their own data.
 */
public interface CardService {
    /**
     * Returns card by id.
     * Only ADMIN is allowed to perform this operation.
     *
     * @param id card id
     * @return card DTO
     * @throws ResourceNotFoundException if card with id does not exist
     */
    CardDto getCardById(Long id);

    /**
     * Returns paginated list of all cards (ADMIN only).
     *
     * @param pageable pagination info
     * @return page of cards
     */
    Page<CardDto> getAll(Pageable pageable);

    /**
     * Returns card belonging to a specific user.
     * Only the user himself is allowed to perform this operation.
     *
     * @param id       card id
     * @param username username of the cardholder
     * @return card entity
     * @throws ResourceNotFoundException if card not found for given user
     */
    Card getCardByIdAndUser(Long id, String username);

    /**
     * Returns paginated list of cards for a specific user.
     *
     * @param pageable pagination info
     * @param username username
     * @return page of user's cards
     */
    Page<CardDto> getAllByUser(Pageable pageable, String username);

    /**
     * Returns balance of a specific card.
     *
     * @param id       card id
     * @param username user's username
     * @return card balance
     * @throws ResourceNotFoundException if card not found
     */
    BigDecimal getCardBalance(Long id, String username);

    /**
     * Returns total balance across all user's cards.
     *
     * @param username username
     * @return total balance
     */
    BigDecimal getUserBalance(String username);

    /**
     * Creates a new card for a user (ADMIN only).
     *
     * @param card     card entity (id will be ignored if present)
     * @param holderId cardholder id
     * @return persisted card
     * @throws ResourceNotFoundException if user does not exist
     */
    Card createCard(Card card, Long holderId);

    /**
     * Activates a card (ADMIN only).
     *
     * @param id card id
     * @throws ResourceNotFoundException if card does not exist
     */
    void activateCard(Long id);

    /**
     * Blocks the card (ADMIN only).
     *
     * @param id card id
     * @throws ResourceNotFoundException if card does not exist
     */
    void blockCard(Long id);

    /**
     * Deletes the card (ADMIN only).
     *
     * @param id card id
     * @throws ResourceNotFoundException if card does not exist
     */
    void deleteCard(Long id);
}
