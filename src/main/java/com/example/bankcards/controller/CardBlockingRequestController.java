package com.example.bankcards.controller;

import com.example.bankcards.dto.PageRequestDto;
import com.example.bankcards.entity.CardBlockingRequest;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.service.CardBlockingRequestService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/requests/block")
public class CardBlockingRequestController {
    private final CardBlockingRequestService cardBlockingRequestService;

    // ===================== GET =====================
    @GetMapping
    @Operation(summary = "Get all card blocking requests (ADMIN)")
    public ResponseEntity<Page<CardBlockingRequest>> getCardBlockingRequests(@ModelAttribute PageRequestDto pageRequestDto) {
        if (pageRequestDto.getSortBy() == null) {
            pageRequestDto.setSortBy("createdAt");
        }
        Page<CardBlockingRequest> requestsPage = cardBlockingRequestService.getAll(pageRequestDto.toPageable());
        return ResponseEntity.ok().body(requestsPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get card blocking request by ID (ADMIN)")
    public ResponseEntity<CardBlockingRequest> getCardBlockingRequest(@PathVariable Long id) {
        CardBlockingRequest cardBlockingRequest = cardBlockingRequestService.getById(id);
        return ResponseEntity.ok(cardBlockingRequest);
    }

    // ===================== CREATE =====================
    @PostMapping("/{id}")
    @Operation(summary = "Create a card blocking request (USER)")
    public ResponseEntity<CardBlockingRequest> addCardBlockRequest(@PathVariable("id") Long cardId,
                                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        cardBlockingRequestService.createCardBlockRequest(cardId, username);
        return ResponseEntity.noContent().build();
    }
}
