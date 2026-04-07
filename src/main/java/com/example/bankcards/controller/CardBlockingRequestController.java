package com.example.bankcards.controller;

import com.example.bankcards.entity.CardBlockingRequest;
import com.example.bankcards.service.CardBlockingRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/requests/block")
public class CardBlockingRequestController {
    private final CardBlockingRequestService cardBlockingRequestService;

    @GetMapping
    public ResponseEntity<List<CardBlockingRequest>> getCardBlockingRequests() {
        List<CardBlockingRequest> requests = cardBlockingRequestService.getAll()
                .stream()
                .collect(toList());
        return ResponseEntity.ok().body(requests);
    }

    @PostMapping("/{id}")
    public ResponseEntity<CardBlockingRequest> addCardBlockRequest(@PathVariable("id") Long cardId, Principal principal) {
        String username = principal.getName();
        cardBlockingRequestService.createCardBlockRequest(cardId, username);
        return ResponseEntity.noContent().build();
    }
}
