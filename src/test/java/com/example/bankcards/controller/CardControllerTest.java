package com.example.bankcards.controller;

import com.example.bankcards.TestConfig;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.mappers.CardMapper;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.validators.CardValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import tools.jackson.databind.ObjectMapper;

import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
class CardControllerTest {
    public static final String BASE_URL = "/api/cards";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private CardMapper cardMapper;

    @MockitoBean
    private CardValidator cardValidator;

    private CardDto getDto() {
        return CardDto.builder()
                .id(1L)
                .number("**** **** **** 1234")
                .build();
    }

    // ===================== GET /api/cards =====================

    @Test
    void getCards_shouldReturn200_forAdmin() throws Exception {
        when(cardService.getAll(any()))
                .thenReturn(new PageImpl<>(List.of(getDto())));

        mockMvc.perform(get(BASE_URL)
                        .with(user("admin").authorities(
                                new SimpleGrantedAuthority("ADMIN")
                        )))
                .andExpect(status().isOk());

        verify(cardService).getAll(any());
    }

    // ===================== GET /api/cards/{id} =====================

    @Test
    void getCard_shouldReturn200_forAdmin() throws Exception {
        when(cardService.getCardById(1L))
                .thenReturn(getDto());

        mockMvc.perform(get(BASE_URL + "/1")
                        .with(user("admin").authorities(
                                new SimpleGrantedAuthority("ADMIN")
                        )))
                .andExpect(status().isOk());

        verify(cardService).getCardById(1L);
    }

    @Test
    void getCard_shouldReturn400_whenNotFound() throws Exception {
        when(cardService.getCardById(1L))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get(BASE_URL + "/1")
                        .with(user("admin").authorities(
                                new SimpleGrantedAuthority("ADMIN")
                        )))
                .andExpect(status().isNotFound());

        verify(cardService).getCardById(1L);
    }

    // ===================== POST /api/cards/ =====================

    @Test
    void addCard_shouldReturn201() throws Exception {
        CreateCardRequest request = new CreateCardRequest();
        request.setHolderId(1L);
        request.setNumber("1234123412341234");
        request.setExpiry(YearMonth.now().plusYears(1));

        Card card = new Card();
        Card created = new Card();
        created.setId(10L);

        CardDto dto = CardDto.builder().id(10L).build();

        when(cardMapper.requestToEntity(any())).thenReturn(card);
        doNothing().when(cardValidator).validate(any(), any());
        when(cardService.createCard(any(), eq(1L))).thenReturn(created);
        when(cardMapper.toDto(created)).thenReturn(dto);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/cards/10"))
                .andExpect(jsonPath("$.id").value(10));

        verify(cardService).createCard(card, 1L);
    }

    @Test
    void addCard_shouldReturn400_whenValidationFails() throws Exception {
        CreateCardRequest request = new CreateCardRequest();

        Card card = new Card();

        when(cardMapper.requestToEntity(any())).thenReturn(card);

        doAnswer(invocation -> {
            BindingResult br = invocation.getArgument(1);
            br.rejectValue("number", "error", "invalid");
            return null;
        }).when(cardValidator).validate(any(), any());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).createCard(any(), any());
    }

    // ===================== PATCH /api/cards/{id}/activate =====================

    @Test
    void activateCard_shouldReturn204() throws Exception {
        doNothing().when(cardService).activateCard(1L);

        mockMvc.perform(patch(BASE_URL + "/1/activate"))
                .andExpect(status().isNoContent());

        verify(cardService).activateCard(1L);
    }

    @Test
    void activateCard_shouldReturn400_whenException() throws Exception {
        doThrow(new AppRuntimeException("Card is already active!"))
                .when(cardService).activateCard(1L);

        mockMvc.perform(patch(BASE_URL + "/1/activate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Card is already active!"));

        verify(cardService).activateCard(1L);
    }

    // ===================== PATCH /api/cards/{id}/block =====================

    @Test
    void blockCard_shouldReturn204() throws Exception {
        doNothing().when(cardService).blockCard(1L);

        mockMvc.perform(patch(BASE_URL + "/1/block"))
                .andExpect(status().isNoContent());

        verify(cardService).blockCard(1L);
    }

    @Test
    void blockCard_shouldReturn400_whenException() throws Exception {
        doThrow(new AppRuntimeException("Card is already blocked!"))
                .when(cardService).blockCard(1L);

        mockMvc.perform(patch("/api/cards/1/block"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Card is already blocked!"));
    }

    // ===================== DELETE /api/cards/{id} =====================

    @Test
    void deleteCard_shouldReturnNoContent() throws Exception {
        doNothing().when(cardService).deleteCard(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCard_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Card with id 99 does not exist"))
                .when(cardService).deleteCard(99L);

        mockMvc.perform(delete(BASE_URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card with id 99 does not exist"));
    }

    // ===================== ExceptionHandler =====================

    @Test
    void shouldHandleAppRuntimeException() throws Exception {
        when(cardService.getCardById(1L))
                .thenThrow(new AppRuntimeException("Error"));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error"));
    }
}
