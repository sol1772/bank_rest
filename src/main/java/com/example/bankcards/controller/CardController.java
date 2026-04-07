package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.mappers.CardMapper;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.validators.CardValidator;
import com.example.bankcards.util.AppErrorResponse;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.util.ErrorsUtil.returnErrorsToClient;
import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;
    private final CardValidator cardValidator;
    private final CardMapper mapper = Mappers.getMapper(CardMapper.class);

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCard(@PathVariable("id") Long id) {
        Card card = cardService.getCardById(id);
        return ResponseEntity.of(Optional.ofNullable(mapper.toDto(card)));
    }

    @GetMapping
    public ResponseEntity<List<CardDto>> getCards() {
        List<CardDto> cards = cardService.getAll()
                .stream()
                .map(mapper::toDto)
                .collect(toList());
        return ResponseEntity.ok().body(cards);
    }

    @GetMapping("/user")
    public ResponseEntity<List<CardDto>> getUserCards(Principal principal) {
        String username = principal.getName();
        List<CardDto> cards = cardService.getAllByUser(username)
                .stream()
                .map(mapper::toDto)
                .collect(toList());
        return ResponseEntity.ok().body(cards);
    }

    @GetMapping("/balance/{id}")
    public ResponseEntity<BigDecimal> getCardBalance(@PathVariable("id") Long id, Principal principal) {
        String username = principal.getName();
        BigDecimal balance = cardService.getCardBalance(id, username);
        return ResponseEntity.of(Optional.ofNullable(balance));
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getUserBalance(Principal principal) {
        String username = principal.getName();
        BigDecimal balance = cardService.getUserBalance(username);
        return ResponseEntity.of(Optional.ofNullable(balance));
    }

    @PostMapping("/add")
    public ResponseEntity<CardDto> addCard(@RequestBody CardDto cardDto, String password, BindingResult bindingResult) {
        Card card = mapper.toEntity(cardDto);
        cardValidator.validate(card, bindingResult);
        if (bindingResult.hasErrors()) {
            returnErrorsToClient(bindingResult);
        }
        Card created = cardService.createCard(card);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    @PostMapping("/activate/{id}")
    public void activateCard(@PathVariable("id") Long id) {
        cardService.activateCard(id);
    }

    @PostMapping("/block/{id}")
    public void blockCard(@PathVariable("id") Long id) {
        cardService.blockCard(id);
    }

    @DeleteMapping("/{id}")
    public void deleteCard(@PathVariable("id") long id) {
        cardService.deleteCard(id);
    }

    @ExceptionHandler
    private ResponseEntity<AppErrorResponse> handleException(AppRuntimeException e) {
        AppErrorResponse response = new AppErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
