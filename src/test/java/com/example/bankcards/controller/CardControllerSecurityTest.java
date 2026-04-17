package com.example.bankcards.controller;

import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static com.example.bankcards.controller.CustomSecurityMockMvcRequestPostProcessors.customUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerSecurityTest {
    public static final String BASE_URL = "/api/cards";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    // ===================== GET /api/cards/me =====================

    @Test
    void getMyCards_shouldReturn200() throws Exception {
        when(cardService.getAllByUser(any(), eq("Alice")))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get(BASE_URL + "/me")
                        .with(customUser(1L, "Alice")))
                .andExpect(status().isOk());

        verify(cardService).getAllByUser(any(), eq("Alice"));
    }

    // ===================== GET /api/cards/{id}/balance =====================

    @Test
    void getCardBalance_shouldReturn200() throws Exception {
        when(cardService.getCardBalance(1L, "Alice"))
                .thenReturn(BigDecimal.TEN);

        mockMvc.perform(get(BASE_URL + "/1/balance")
                        .with(customUser(1L, "Alice")))
                .andExpect(status().isOk());

        verify(cardService).getCardBalance(1L, "Alice");
    }

    @Test
    void getCardBalance_shouldReturn404_whenNull() throws Exception {
        when(cardService.getCardBalance(1L, "Alice"))
                .thenReturn(null);

        mockMvc.perform(get(BASE_URL + "/1/balance")
                        .with(customUser(1L, "Alice")))
                .andExpect(status().isNotFound());

        verify(cardService).getCardBalance(1L, "Alice");
    }

    // ===================== GET /api/cards/me/balance =====================

    @Test
    void getUserBalance_shouldReturn200() throws Exception {
        when(cardService.getUserBalance("Alice"))
                .thenReturn(BigDecimal.TEN);

        mockMvc.perform(get(BASE_URL + "/me/balance")
                        .with(customUser(1L, "Alice")))
                .andExpect(status().isOk());

        verify(cardService).getUserBalance("Alice");
    }

    @Test
    void getUserBalance_shouldReturn404_whenNull() throws Exception {
        when(cardService.getUserBalance("Alice"))
                .thenReturn(null);

        mockMvc.perform(get(BASE_URL + "/me/balance")
                        .with(customUser(1L, "Alice")))
                .andExpect(status().isNotFound());

        verify(cardService).getUserBalance("Alice");
    }
}
