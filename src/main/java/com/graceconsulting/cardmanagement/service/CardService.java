package com.graceconsulting.cardmanagement.service;

import com.graceconsulting.cardmanagement.dto.BatchUploadResponse;
import com.graceconsulting.cardmanagement.dto.CardRequest;
import com.graceconsulting.cardmanagement.dto.CardResponse;
import com.graceconsulting.cardmanagement.dto.CardSearchResponse;
import com.graceconsulting.cardmanagement.entity.Card;
import com.graceconsulting.cardmanagement.enums.BatchCardResult;
import com.graceconsulting.cardmanagement.exception.BusinessException;
import com.graceconsulting.cardmanagement.exception.ResourceConflictException;
import com.graceconsulting.cardmanagement.mapper.CardMapper;
import com.graceconsulting.cardmanagement.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    @Transactional
    public CardResponse createCard(CardRequest request) {
        String cardNumber = cardMapper.normalizeCardNumber(request.cardNumber());
        log.info("Criando novo cartão");

        String hash = cardMapper.hashCardNumber(cardNumber);

        if (cardRepository.existsByCardNumberHash(hash)) {
            throw new ResourceConflictException("Cartão já cadastrado no sistema");
        }

        Card card = cardMapper.toEntity(request);
        Card savedCard = cardRepository.save(card);

        log.info("Cartão criado com ID: {}", savedCard.getId());
        return cardMapper.toResponse(savedCard, cardNumber);
    }

    @Transactional(readOnly = true)
    public CardSearchResponse searchCard(String cardNumber) {
        String normalizedNumber = cardMapper.normalizeCardNumber(cardNumber);
        log.info("Buscando cartão");

        String hash = cardMapper.hashCardNumber(normalizedNumber);

        return cardRepository.findByCardNumberHash(hash)
                .map(card -> CardSearchResponse.found(card.getId()))
                .orElse(CardSearchResponse.notFound());
    }

    @Transactional
    public BatchUploadResponse processBatchFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("Arquivo vazio");
        }

        log.info("Processando arquivo em lote: {}", file.getOriginalFilename());

        String batchId = UUID.randomUUID().toString();
        int totalProcessed = 0;
        int successCount = 0;
        int duplicateCount = 0;
        int errorCount = 0;
        List<BatchUploadResponse.BatchItemError> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Ignora header e linhas vazias
                if (isHeaderOrFooterLine(line, lineNumber)) {
                    continue;
                }

                String cardNumber = extractCardNumber(line);
                if (cardNumber == null || cardNumber.isEmpty()) {
                    continue;
                }

                totalProcessed++;

                try {
                    BatchCardResult result = saveCardFromBatch(cardNumber, batchId);

                    switch (result) {
                        case INVALID -> {
                            errorCount++;
                            errors.add(new BatchUploadResponse.BatchItemError(
                                    lineNumber,
                                    maskCardNumber(cardNumber),
                                    "Cartão inválido"
                            ));
                            log.warn("Linha {}: Cartão inválido", lineNumber);
                        }
                        case DUPLICATE -> {
                            duplicateCount++;
                            errors.add(new BatchUploadResponse.BatchItemError(
                                    lineNumber,
                                    maskCardNumber(cardNumber),
                                    "Cartão já cadastrado no sistema"
                            ));
                            log.warn("Linha {}: Cartão já cadastrado", lineNumber);
                        }
                        case SUCCESS -> successCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                    errors.add(new BatchUploadResponse.BatchItemError(
                            lineNumber,
                            maskCardNumber(cardNumber),
                            e.getMessage()
                    ));
                    log.warn("Erro ao processar linha {}: {}", lineNumber, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Erro ao processar arquivo: {}", e.getMessage());
            throw new BusinessException("Erro ao processar arquivo: " + e.getMessage());
        }

        log.info("Lote {} processado: {} total, {} sucesso, {} duplicados, {} erros",
                batchId, totalProcessed, successCount, duplicateCount, errorCount);

        return new BatchUploadResponse(batchId, totalProcessed, successCount, duplicateCount, errorCount, errors);
    }

    private boolean isHeaderOrFooterLine(String line, int lineNumber) {
        if (line.trim().isEmpty()) {
            return true;
        }
        // Header começa com nome do arquivo ou "DESAFIO"
        if (lineNumber == 1 || line.startsWith("DESAFIO")) {
            return true;
        }
        // Footer começa com "LOTE"
        if (line.startsWith("LOTE")) {
            return true;
        }
        return false;
    }

    private String extractCardNumber(String line) {
        // Formato: [01-01]ID [02-07]NUM_LOTE [08-26]CARTAO
        // Exemplo: C2     4456897999999999
        if (line.length() >= 22) {
            return line.substring(6, Math.min(line.length(), 26)).trim();
        }
        return null;
    }

    private BatchCardResult saveCardFromBatch(String cardNumber, String batchId) {
        if (!isValidCardNumber(cardNumber)) {
            return BatchCardResult.INVALID;
        }

        String hash = cardMapper.hashCardNumber(cardNumber);

        if (cardRepository.existsByCardNumberHash(hash)) {
            return BatchCardResult.DUPLICATE;
        }

        Card card = cardMapper.toEntity(cardNumber, batchId);
        cardRepository.save(card);
        return BatchCardResult.SUCCESS;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        return cardNumber.substring(0, 4) + "****" + cardNumber.substring(cardNumber.length() - 4);
    }

    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }
        // Verifica se contém apenas dígitos
        if (!cardNumber.matches("^\\d+$")) {
            return false;
        }
        // Verifica tamanho (13 a 19 dígitos)
        int length = cardNumber.length();
        return length >= 13 && length <= 19;
    }

}
