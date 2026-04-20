package com.example.bankcards.controller;

import com.example.bankcards.TestConfig;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.mappers.UserMapper;
import com.example.bankcards.dto.request.ChangePasswordRequest;
import com.example.bankcards.dto.request.ChangeRoleRequest;
import com.example.bankcards.dto.request.CreateUserRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.AppRuntimeException;
import com.example.bankcards.service.UserService;
import com.example.bankcards.service.validators.UserValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security
@Import(TestConfig.class)
class UserControllerTest {
    public static final String BASE_URL = "/api/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserValidator userValidator;

    @MockitoBean
    private UserMapper userMapper;

    // ===================== GET /api/users =====================

    @Test
    void getUsers_shouldReturnUsersPage() throws Exception {
        Page<UserDto> page = new PageImpl<>(List.of(new UserDto()));

        when(userService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL)).andExpect(status().isOk());
    }

    // ===================== GET /api/users/{id} =====================

    @Test
    void getUser_shouldReturnUser() throws Exception {
        User user = new User();
        user.setId(1L);

        UserDto dto = new UserDto();

        when(userService.getUserById(1L)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getUser_shouldReturn404_whenUserNotFound() throws Exception {
        when(userService.getUserById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());
    }

    // ===================== POST /api/users/ =====================

    @Test
    void addUser_shouldCreateUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("test_user");
        request.setPassword("Pass12345");
        request.setRole(Role.USER);

        User user = new User();
        user.setId(1L);

        UserDto dto = new UserDto();

        when(userMapper.requestToEntity(any())).thenReturn(user);
        when(userService.createUser(any(), any())).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        verify(userService).createUser(any(), eq("Pass12345"));
    }

    @Test
    void addUser_shouldReturnBadRequest_whenValidationFails() throws Exception {
        CreateUserRequest request = new CreateUserRequest();

        User user = new User();

        when(userMapper.requestToEntity(any())).thenReturn(user);

        doAnswer(invocation -> {
            BindingResult br = invocation.getArgument(1);
            br.rejectValue("username", "error", "Username is already in use");
            return null;
        }).when(userValidator).validate(any(), any());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===================== PATCH /api/users/{id}/role =====================

    @Test
    void changeRole_shouldReturnUpdatedUser() throws Exception {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setRole(Role.ADMIN);
        User user = new User();
        UserDto dto = new UserDto();

        when(userService.changeRole(eq(1L), eq(Role.ADMIN))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        mockMvc.perform(patch(BASE_URL + "/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ===================== PATCH /api/users/{id}/password =====================

    @Test
    void changePassword_shouldReturnUpdatedUser() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("old");
        request.setNewPassword("new");

        User user = new User();
        UserDto dto = new UserDto();

        when(userService.changePassword(eq(1L), eq("old"), eq("new")))
                .thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        mockMvc.perform(patch(BASE_URL + "/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ===================== PATCH /api/users/{id}/reset-password =====================

    @Test
    void resetPassword_shouldReturnUpdatedUser() throws Exception {
        User user = new User();
        UserDto dto = new UserDto();

        when(userService.resetPassword(1L, "123")).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        mockMvc.perform(patch(BASE_URL + "/1/reset-password")
                        .param("newPassword", "123"))
                .andExpect(status().isOk());
    }

    // ===================== DELETE /api/users/{id} =====================

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
    }

    // ===================== ExceptionHandler =====================

    @Test
    void shouldHandleAppRuntimeException() throws Exception {
        when(userService.getUserById(1L))
                .thenThrow(new AppRuntimeException("Error"));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error"));
    }
}