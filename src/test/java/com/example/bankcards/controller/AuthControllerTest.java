package com.example.bankcards.controller;

import com.example.bankcards.TestConfig;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
class AuthControllerTest {
    public static final String BASE_URL = "/api/auth";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void login_shouldReturn200() throws Exception {
        String json = """
                {
                  "username": "Alice",
                  "password": "password"
                }
                """;

        AuthResponse response = new AuthResponse("jwt-token", "Alice", "USER");

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("Alice"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authService).login(any());
    }

    @Test
    void login_shouldReturn400_whenFieldsEmpty() throws Exception {
        String json = """
                {
                  "username": "",
                  "password": ""
                }
                """;

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void login_shouldReturn401_whenServiceThrows() throws Exception {
        String json = """
                {
                  "username": "Alice",
                  "password": "wrong"
                }
                """;

        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
}