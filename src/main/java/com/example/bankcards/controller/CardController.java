package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.PageRequestDto;
import com.example.bankcards.dto.mappers.CardMapper;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.validators.CardValidator;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Optional;

import static com.example.bankcards.util.ErrorsUtil.returnErrorsToClient;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;
    private final CardMapper cardMapper;
    private final CardValidator cardValidator;

    // ===================== GET =====================
    @GetMapping
    @Operation(summary = "Get all cards (ADMIN)")
    public ResponseEntity<Page<CardDto>> getCards(@ModelAttribute PageRequestDto pageRequestDto) {
        if (pageRequestDto.getSortBy() == null) {
            pageRequestDto.setSortBy("holder");
        }
        Page<CardDto> cardsPage = cardService.getAll(pageRequestDto.toPageable());
        return ResponseEntity.ok().body(cardsPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get card by ID (ADMIN)")
    public ResponseEntity<CardDto> getCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Get own card (USER)")
    public ResponseEntity<Page<CardDto>> getMyCards(@ModelAttribute PageRequestDto pageRequestDto,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        if (pageRequestDto.getSortBy() == null) {
            pageRequestDto.setSortBy("number");
        }
        Page<CardDto> cardsPage = cardService.getAllByUser(pageRequestDto.toPageable(), username);
        return ResponseEntity.ok(cardsPage);
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get own card balance (USER)")
    public ResponseEntity<BigDecimal> getCardBalance(@PathVariable Long id,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        BigDecimal balance = cardService.getCardBalance(id, username);
        if (balance == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/me/balance")
    @Operation(summary = "Get balance (USER)")
    public ResponseEntity<BigDecimal> getUserBalance(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        BigDecimal balance = cardService.getUserBalance(username);
        return ResponseEntity.of(Optional.ofNullable(balance));
    }

    // ===================== CREATE =====================
    @PostMapping
    @Operation(summary = "Create card (ADMIN)")
    public ResponseEntity<CardDto> addCard(@Valid @RequestBody CreateCardRequest request, BindingResult bindingResult) {
        Card card = cardMapper.requestToEntity(request);
        cardValidator.validate(card, bindingResult);
        if (bindingResult.hasErrors()) {
            returnErrorsToClient(bindingResult);
        }
        Card created = cardService.createCard(card, request.getHolderId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(cardMapper.toDto(created));
    }

    // ===================== UPDATE =====================
    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate card (ADMIN)")
    public ResponseEntity<Void> activateCard(@PathVariable Long id) {
        cardService.activateCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Block card (ADMIN)")
    public ResponseEntity<Void> blockCard(@PathVariable Long id) {
        cardService.blockCard(id);
        return ResponseEntity.noContent().build();
    }

    // ===================== DELETE =====================
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a card (ADMIN)")
    public ResponseEntity<Void> deleteCard(@PathVariable long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
