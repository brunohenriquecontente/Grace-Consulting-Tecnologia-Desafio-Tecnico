package com.graceconsulting.cardmanagement.service;

import com.graceconsulting.cardmanagement.dto.BatchUploadResponse;
import com.graceconsulting.cardmanagement.dto.CardRequest;
import com.graceconsulting.cardmanagement.dto.CardResponse;
import com.graceconsulting.cardmanagement.dto.CardSearchResponse;
import com.graceconsulting.cardmanagement.entity.Card;
import com.graceconsulting.cardmanagement.exception.BusinessException;
import com.graceconsulting.cardmanagement.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;

    @Transactional
    public CardResponse createCard(CardRequest request) {
        String cardNumber = normalizeCardNumber(request.cardNumber());
        log.info("Criando novo cartão");

        String hash = encryptionService.hash(cardNumber);

        if (cardRepository.existsByCardNumberHash(hash)) {
            throw new BusinessException("Cartão já cadastrado no sistema");
        }

        Card card = Card.builder()
                .cardNumberEncrypted(encryptionService.encrypt(cardNumber))
                .cardNumberHash(hash)
                .build();

        Card savedCard = cardRepository.save(card);
        log.info("Cartão criado com ID: {}", savedCard.getId());

        return toCardResponse(savedCard, cardNumber);
    }

    @Transactional(readOnly = true)
    public CardSearchResponse searchCard(String cardNumber) {
        String normalizedNumber = normalizeCardNumber(cardNumber);
        log.info("Buscando cartão");

        String hash = encryptionService.hash(normalizedNumber);

        return cardRepository.findByCardNumberHash(hash)
                .map(card -> CardSearchResponse.found(card.getId()))
                .orElse(CardSearchResponse.notFound());
    }

    @Transactional
    public BatchUploadResponse processBatchFile(MultipartFile file) {
        log.info("Processando arquivo em lote: {}", file.getOriginalFilename());

        String batchId = UUID.randomUUID().toString();
        int totalProcessed = 0;
        int successCount = 0;
        int errorCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Ignora cabeçalho e linhas vazias
                if (lineNumber == 1 || line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String cardNumber = extractCardNumber(line);
                    if (cardNumber != null && !cardNumber.isEmpty()) {
                        totalProcessed++;
                        saveCardFromBatch(cardNumber, batchId);
                        successCount++;
                    }
                } catch (Exception e) {
                    totalProcessed++;
                    errorCount++;
                    log.warn("Erro ao processar linha {}: {}", lineNumber, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Erro ao processar arquivo: {}", e.getMessage());
            throw new BusinessException("Erro ao processar arquivo: " + e.getMessage());
        }

        log.info("Lote {} processado: {} total, {} sucesso, {} erros",
                batchId, totalProcessed, successCount, errorCount);

        return new BatchUploadResponse(batchId, totalProcessed, successCount, errorCount);
    }

    private String extractCardNumber(String line) {
        // Formato esperado: posição fixa conforme DESAFIO-HYPERATIVA.txt
        // O número do cartão começa na posição 6 e tem 16 dígitos
        if (line.length() >= 22) {
            String cardNumber = line.substring(6, 22).trim();
            // Remove caracteres não numéricos
            return cardNumber.replaceAll("\\D", "");
        }
        return null;
    }

    private void saveCardFromBatch(String cardNumber, String batchId) {
        String normalizedNumber = normalizeCardNumber(cardNumber);
        String hash = encryptionService.hash(normalizedNumber);

        if (!cardRepository.existsByCardNumberHash(hash)) {
            Card card = Card.builder()
                    .cardNumberEncrypted(encryptionService.encrypt(normalizedNumber))
                    .cardNumberHash(hash)
                    .batchId(batchId)
                    .build();
            cardRepository.save(card);
        }
    }

    private String normalizeCardNumber(String cardNumber) {
        return cardNumber.replaceAll("\\s+", "").replaceAll("-", "");
    }

    private CardResponse toCardResponse(Card card, String originalNumber) {
        return new CardResponse(
                card.getId(),
                encryptionService.maskCardNumber(originalNumber),
                card.getCreatedAt()
        );
    }
}
