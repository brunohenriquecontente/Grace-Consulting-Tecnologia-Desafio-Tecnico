package com.graceconsulting.cardmanagement.mapper;

import com.graceconsulting.cardmanagement.dto.CardRequest;
import com.graceconsulting.cardmanagement.dto.CardResponse;
import com.graceconsulting.cardmanagement.entity.Card;
import com.graceconsulting.cardmanagement.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardMapper Tests")
class CardMapperTest {

    @Mock
    private EncryptionService encryptionService;

    private CardMapper cardMapper;

    @BeforeEach
    void setUp() {
        cardMapper = new CardMapperImpl();
        ReflectionTestUtils.setField(cardMapper, "encryptionService", encryptionService);
    }

    @Test
    @DisplayName("Deve converter CardRequest para Card")
    void shouldConvertCardRequestToCard() {
        CardRequest request = new CardRequest("4111111111111111");

        when(encryptionService.encrypt(anyString())).thenReturn("encrypted_data");
        when(encryptionService.hash(anyString())).thenReturn("hash_value");

        Card result = cardMapper.toEntity(request);

        assertNotNull(result);
        assertEquals("encrypted_data", result.getCardNumberEncrypted());
        assertEquals("hash_value", result.getCardNumberHash());
    }

    @Test
    @DisplayName("Deve converter cardNumber e batchId para Card")
    void shouldConvertCardNumberAndBatchIdToCard() {
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted");
        when(encryptionService.hash(anyString())).thenReturn("hash");

        Card result = cardMapper.toEntity("4111111111111111", "batch-123");

        assertNotNull(result);
        assertEquals("encrypted", result.getCardNumberEncrypted());
        assertEquals("hash", result.getCardNumberHash());
        assertEquals("batch-123", result.getBatchId());
    }

    @Test
    @DisplayName("Deve converter Card para CardResponse")
    void shouldConvertCardToCardResponse() {
        UUID randomCardId = UUID.randomUUID();
        Card card = Card.builder()
                .id(randomCardId)
                .cardNumberEncrypted("encrypted")
                .cardNumberHash("hash")
                .createdAt(LocalDateTime.now())
                .build();

        when(encryptionService.maskCardNumber("4111111111111111")).thenReturn("4111****1111");

        CardResponse result = cardMapper.toResponse(card, "4111111111111111");

        assertNotNull(result);
        assertEquals(randomCardId, result.id());
        assertEquals("4111****1111", result.maskedNumber());
        assertNotNull(result.createdAt());
    }

    @Test
    @DisplayName("Deve normalizar número do cartão removendo espaços")
    void shouldNormalizeCardNumberRemovingSpaces() {
        String result = cardMapper.normalizeCardNumber("4111 1111 1111 1111");

        assertEquals("4111111111111111", result);
    }

    @Test
    @DisplayName("Deve normalizar número do cartão removendo hífens")
    void shouldNormalizeCardNumberRemovingHyphens() {
        String result = cardMapper.normalizeCardNumber("4111-1111-1111-1111");

        assertEquals("4111111111111111", result);
    }

    @Test
    @DisplayName("Deve gerar hash do número do cartão normalizado")
    void shouldHashNormalizedCardNumber() {
        when(encryptionService.hash("4111111111111111")).thenReturn("hash_value");

        String result = cardMapper.hashCardNumber("4111 1111 1111 1111");

        assertEquals("hash_value", result);
    }
}
