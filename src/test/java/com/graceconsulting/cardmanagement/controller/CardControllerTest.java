package com.graceconsulting.cardmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graceconsulting.cardmanagement.dto.*;
import com.graceconsulting.cardmanagement.exception.BusinessException;
import com.graceconsulting.cardmanagement.exception.GlobalExceptionHandler;
import com.graceconsulting.cardmanagement.exception.ResourceConflictException;
import com.graceconsulting.cardmanagement.security.JwtAuthenticationFilter;
import com.graceconsulting.cardmanagement.security.JwtTokenProvider;
import com.graceconsulting.cardmanagement.service.CardService;
import com.graceconsulting.cardmanagement.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class})
@DisplayName("CardController Tests")
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Deve criar cartão com sucesso")
    void shouldCreateCardSuccessfully() throws Exception {
        UUID randomCardId = UUID.randomUUID();
        CardRequest request = new CardRequest("4111111111111111");
        CardResponse response = new CardResponse(randomCardId, "4111****1111", LocalDateTime.now());

        when(cardService.createCard(any(CardRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(randomCardId.toString()))
                .andExpect(jsonPath("$.maskedNumber").value("4111****1111"));
    }

    @Test
    @DisplayName("Deve retornar 400 para número de cartão inválido")
    void shouldReturn400ForInvalidCardNumber() throws Exception {
        CardRequest request = new CardRequest("123");

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict para cartão duplicado")
    void shouldReturn409ForDuplicateCard() throws Exception {
        CardRequest request = new CardRequest("4111111111111111");

        when(cardService.createCard(any(CardRequest.class)))
                .thenThrow(new ResourceConflictException("Cartão já cadastrado no sistema"));

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve buscar cartão existente")
    void shouldSearchExistingCard() throws Exception {
        UUID randomCardId = UUID.randomUUID();

        CardSearchRequest request = new CardSearchRequest("4111111111111111");
        CardSearchResponse response = CardSearchResponse.found(randomCardId);

        when(cardService.searchCard(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/cards/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.id").value(randomCardId.toString()));
    }

    @Test
    @DisplayName("Deve retornar not found para cartão inexistente")
    void shouldReturnNotFoundForNonExistingCard() throws Exception {
        CardSearchRequest request = new CardSearchRequest("4222222222222222");
        CardSearchResponse response = CardSearchResponse.notFound();

        when(cardService.searchCard(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/cards/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(false))
                .andExpect(jsonPath("$.id").isEmpty());
    }

    @Test
    @DisplayName("Deve processar upload em lote")
    void shouldProcessBatchUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cards.txt",
                "text/plain",
                "header\n0000014111111111111111".getBytes()
        );

        BatchUploadResponse response = new BatchUploadResponse("batch-id", 1, 1, 0, 0, java.util.List.of());

        when(cardService.processBatchFile(any())).thenReturn(response);

        mockMvc.perform(multipart("/api/cards/batch")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batchId").value("batch-id"))
                .andExpect(jsonPath("$.totalProcessed").value(1))
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.errorCount").value(0));
    }

    @Test
    @DisplayName("Deve retornar 400 para arquivo vazio")
    void shouldReturn400ForEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cards.txt",
                "text/plain",
                new byte[0]
        );

        when(cardService.processBatchFile(any()))
                .thenThrow(new BusinessException("Arquivo vazio"));

        mockMvc.perform(multipart("/api/cards/batch")
                        .file(file))
                .andExpect(status().isBadRequest());
    }
}
