package com.example.bankcards.controller;

import com.example.bankcards.dto.BankTransactionDto;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.service.BankTransactionService;
import com.example.bankcards.service.validators.BankTransactionValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.example.bankcards.controller.CustomSecurityMockMvcRequestPostProcessors.customUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BankTransactionControllerTest {
    public static final String BASE_URL = "/api/transactions";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BankTransactionService bankTransactionService;

    @MockitoBean
    private BankTransactionValidator bankTransactionValidator;

    private BankTransactionDto getDto() {
        return BankTransactionDto.builder()
                .transactionReference(UUID.randomUUID().toString())
                .id(1L)
                .fromCardId(1L)
                .toCardId(2L)
                .amount(BigDecimal.TEN)
                .status(TransactionStatus.PENDING)
                .build();
    }

    // ===================== GET /api/transactions =====================

    @Test
    void getCards_shouldReturn200_forAdmin() throws Exception {
        when(bankTransactionService.getAll(any()))
                .thenReturn(new PageImpl<>(List.of(getDto())));

        mockMvc.perform(get(BASE_URL)
                        .with(user("admin").authorities(
                                new SimpleGrantedAuthority("ADMIN")
                        )))
                .andExpect(status().isOk());

        verify(bankTransactionService).getAll(any());
    }

    // ===================== GET /api/transactions/{id} =====================

    @Test
    void getCard_shouldReturn200_forAdmin() throws Exception {
        when(bankTransactionService.getById(1L)).thenReturn(any());

        mockMvc.perform(get(BASE_URL + "/1")
                        .with(user("admin").authorities(
                                new SimpleGrantedAuthority("ADMIN")
                        )))
                .andExpect(status().isOk());

        verify(bankTransactionService).getById(1L);
    }

    // ===================== POST /api/transactions/transfer =====================

    @Test
    void transferFunds_shouldReturn204() throws Exception {
        String json = """
                {
                  "fromCard": {
                    "id": 1,
                    "holder": {
                      "username": "Alice"
                    }
                  },
                  "toCard": {
                    "id": 2,
                    "holder": {
                      "username": "Bob"
                    }
                  },
                  "amount": 100.00
                }
                """;

        mockMvc.perform(post(BASE_URL + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(customUser(1L, "Alice")))
                .andExpect(status().isNoContent());
    }
}