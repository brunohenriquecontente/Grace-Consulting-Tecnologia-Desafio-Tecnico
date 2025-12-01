package com.graceconsulting.cardmanagement.controller;

import com.graceconsulting.cardmanagement.dto.*;
import com.graceconsulting.cardmanagement.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "cards", description = "Endpoints de gerenciamento de cartões")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "register-card", description = "Cadastra um novo cartão de crédito")
    public CardResponse createCard(@Valid @RequestBody CardRequest request) {
        return cardService.createCard(request);
    }

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "check-card", description = "Verifica se um cartão existe e retorna seu ID")
    public CardSearchResponse searchCard(@Valid @RequestBody CardSearchRequest request) {
        return cardService.searchCard(request.cardNumber());
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "batch-upload", description = "Importa cartões a partir de arquivo TXT")
    public BatchUploadResponse uploadBatch(@RequestParam("file") MultipartFile file) {
        return cardService.processBatchFile(file);
    }
}
