package com.example.bankcards.service;

import com.example.bankcards.entity.CardBlockingRequest;
import com.example.bankcards.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for managing card blocking requests.
 */
public interface CardBlockingRequestService {
    /**
     * Returns paginated list of all blocking requests (ADMIN only).
     *
     * @param pageable pagination info
     * @return page of requests
     */
    Page<CardBlockingRequest> getAll(Pageable pageable);

    /**
     * Returns blocking request by id (ADMIN only).
     *
     * @param id request id
     * @return blocking request
     * @throws ResourceNotFoundException if request does not exist
     */
    CardBlockingRequest getById(Long id);

    /**
     * Creates a new card blocking request.
     *
     * @param cardId   card id
     * @param username username of requester
     * @throws ResourceNotFoundException if card does not exist
     */
    void createCardBlockRequest(Long cardId, String username);
}
