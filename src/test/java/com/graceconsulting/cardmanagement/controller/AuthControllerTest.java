package com.graceconsulting.cardmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graceconsulting.cardmanagement.dto.AuthRequest;
import com.graceconsulting.cardmanagement.dto.AuthResponse;
import com.graceconsulting.cardmanagement.dto.UserRegisterRequest;
import com.graceconsulting.cardmanagement.entity.User;
import com.graceconsulting.cardmanagement.exception.GlobalExceptionHandler;
import com.graceconsulting.cardmanagement.security.JwtAuthenticationFilter;
import com.graceconsulting.cardmanagement.security.JwtTokenProvider;
import com.graceconsulting.cardmanagement.service.AuthService;
import com.graceconsulting.cardmanagement.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class})
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void shouldLoginSuccessfully() throws Exception {
        AuthRequest request = new AuthRequest("testuser", "password123");
        AuthResponse response = new AuthResponse("jwt_token", 3600L);

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("Deve retornar 401 para credenciais inválidas")
    void shouldReturn401ForInvalidCredentials() throws Exception {
        AuthRequest request = new AuthRequest("testuser", "wrongpassword");

        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 400 para request sem username")
    void shouldReturn400ForMissingUsername() throws Exception {
        String invalidRequest = "{\"password\": \"password123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 para request sem password")
    void shouldReturn400ForMissingPassword() throws Exception {
        String invalidRequest = "{\"username\": \"testuser\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve registrar usuário com sucesso")
    void shouldRegisterUserSuccessfully() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("newuser", "password123", "New User");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("newuser")
                .name("New User")
                .build();

        when(userService.register(any(UserRegisterRequest.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve retornar 400 para registro com password curto")
    void shouldReturn400ForShortPassword() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("newuser", "12345", "New User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 para registro sem nome")
    void shouldReturn400ForMissingName() throws Exception {
        String invalidRequest = "{\"username\": \"newuser\", \"password\": \"password123\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}
