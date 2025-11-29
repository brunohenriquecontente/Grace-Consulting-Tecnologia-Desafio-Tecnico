package com.graceconsulting.cardmanagement.controller;

import com.graceconsulting.cardmanagement.dto.*;
import com.graceconsulting.cardmanagement.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Cartões", description = "Endpoints de gerenciamento de cartões")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    @PostMapping
    @Operation(summary = "Cadastrar cartão", description = "Cadastra um novo cartão de crédito")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest request) {
        log.info("Requisição de cadastro de cartão");
        CardResponse response = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/search")
    @Operation(summary = "Consultar cartão", description = "Verifica se um cartão existe e retorna seu ID")
    public ResponseEntity<CardSearchResponse> searchCard(@Valid @RequestBody CardSearchRequest request) {
        log.info("Requisição de consulta de cartão");
        CardSearchResponse response = cardService.searchCard(request.cardNumber());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload em lote", description = "Importa cartões a partir de arquivo TXT")
    public ResponseEntity<BatchUploadResponse> uploadBatch(@RequestParam("file") MultipartFile file) {
        log.info("Requisição de upload em lote: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        BatchUploadResponse response = cardService.processBatchFile(file);
        return ResponseEntity.ok(response);
    }
}
