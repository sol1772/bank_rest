package com.example.bankcards.controller;

import com.example.bankcards.service.CardBlockingRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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
class CardBlockingRequestControllerTest {
    public static final String BASE_URL = "/api/requests/block";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardBlockingRequestService blockingRequestService;

    // ===================== GET /api/requests/block =====================

    @Test
    void getCardBlockingRequests_shouldReturn200_forAdmin() throws Exception {
        when(blockingRequestService.getAll(any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get(BASE_URL)
                        .with(user("admin").authorities(
                                new SimpleGrantedAuthority("ADMIN")
                        )))
                .andExpect(status().isOk());

        verify(blockingRequestService).getAll(any());
    }

    // ===================== GET /api/requests/block/{id} =====================

    @Test
    void getCardBlockingRequest_shouldReturn200_forAdmin() throws Exception {
        when(blockingRequestService.getById(1L)).thenReturn(any());

        mockMvc.perform(get(BASE_URL + "/1")
                        .with(user("admin").authorities(
                                new SimpleGrantedAuthority("ADMIN")
                        )))
                .andExpect(status().isOk());

        verify(blockingRequestService).getById(1L);
    }

    // ===================== POST /api/requests/block/1 =====================

    @Test
    void addCardBlockRequest_shouldReturn204() throws Exception {
        String json = """
                  "cardId": 1
                }
                """;

        mockMvc.perform(post(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(customUser(1L, "Alice")))
                .andExpect(status().isNoContent());
    }
}