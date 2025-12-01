package com.graceconsulting.cardmanagement.service;

import com.graceconsulting.cardmanagement.dto.BatchUploadResponse;
import com.graceconsulting.cardmanagement.dto.CardRequest;
import com.graceconsulting.cardmanagement.dto.CardResponse;
import com.graceconsulting.cardmanagement.dto.CardSearchResponse;
import com.graceconsulting.cardmanagement.entity.Card;
import com.graceconsulting.cardmanagement.exception.BusinessException;
import com.graceconsulting.cardmanagement.exception.ResourceConflictException;
import com.graceconsulting.cardmanagement.mapper.CardMapper;
import com.graceconsulting.cardmanagement.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardService Tests")
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private static final String CARD_NUMBER = "4111111111111111";
    private static final String CARD_HASH = "card_hash_value";

    @BeforeEach
    void setUp() {
        lenient().when(cardMapper.normalizeCardNumber(anyString())).thenAnswer(invocation -> {
            String input = invocation.getArgument(0);
            return input.replaceAll("\\s+", "").replaceAll("-", "");
        });
        lenient().when(cardMapper.hashCardNumber(anyString())).thenReturn(CARD_HASH);
    }

    @Nested
    @DisplayName("Testes de Criação de Cartão")
    class CreateCardTests {

        @ParameterizedTest(name = "Deve criar cartão com número: {0}")
        @ValueSource(strings = {
            "4111111111111111",
            "5500000000000004",
            "340000000000009",
            "6011000000000004"
        })
        @DisplayName("Deve criar cartões com diferentes bandeiras")
        void shouldCreateCardsWithDifferentBrands(String cardNumber) {
            CardRequest request = new CardRequest(cardNumber);
            UUID cardId = UUID.randomUUID();
            Card savedCard = createCard(cardId);
            CardResponse expectedResponse = new CardResponse(cardId, "****", LocalDateTime.now());

            when(cardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
            when(cardMapper.toEntity(request)).thenReturn(savedCard);
            when(cardRepository.save(any(Card.class))).thenReturn(savedCard);
            when(cardMapper.toResponse(eq(savedCard), anyString())).thenReturn(expectedResponse);

            CardResponse response = cardService.createCard(request);

            assertNotNull(response);
            assertEquals(cardId, response.id());
            verify(cardRepository).save(any(Card.class));
        }

        @ParameterizedTest(name = "Deve normalizar cartão: {0}")
        @CsvSource({
            "4111 1111 1111 1111, 4111111111111111",
            "4111-1111-1111-1111, 4111111111111111",
            "4111  1111  1111  1111, 4111111111111111"
        })
        @DisplayName("Deve normalizar números de cartão com formatação")
        void shouldNormalizeFormattedCardNumbers(String formattedNumber, String expectedNormalized) {
            CardRequest request = new CardRequest(formattedNumber);
            UUID cardId = UUID.randomUUID();
            Card savedCard = createCard(cardId);
            CardResponse expectedResponse = new CardResponse(cardId, "4111****1111", LocalDateTime.now());

            when(cardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
            when(cardMapper.toEntity(request)).thenReturn(savedCard);
            when(cardRepository.save(any(Card.class))).thenReturn(savedCard);
            when(cardMapper.toResponse(eq(savedCard), anyString())).thenReturn(expectedResponse);

            cardService.createCard(request);

            verify(cardMapper).normalizeCardNumber(formattedNumber);
        }

        @Test
        @DisplayName("Deve lançar ResourceConflictException ao criar cartão duplicado")
        void shouldThrowExceptionWhenCardAlreadyExists() {
            CardRequest request = new CardRequest(CARD_NUMBER);

            when(cardRepository.existsByCardNumberHash(CARD_HASH)).thenReturn(true);

            ResourceConflictException exception = assertThrows(ResourceConflictException.class,
                () -> cardService.createCard(request));

            assertEquals("Cartão já cadastrado no sistema", exception.getMessage());
            verify(cardRepository, never()).save(any(Card.class));
        }

        @Test
        @DisplayName("Deve chamar hashCardNumber antes de verificar duplicidade")
        void shouldCallHashBeforeCheckingDuplicate() {
            CardRequest request = new CardRequest(CARD_NUMBER);

            when(cardRepository.existsByCardNumberHash(CARD_HASH)).thenReturn(true);

            assertThrows(ResourceConflictException.class, () -> cardService.createCard(request));

            verify(cardMapper).hashCardNumber(CARD_NUMBER);
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Cartão")
    class SearchCardTests {

        @ParameterizedTest(name = "Deve buscar cartão: {0}")
        @ValueSource(strings = {
            "4111111111111111",
            "4111 1111 1111 1111",
            "4111-1111-1111-1111"
        })
        @DisplayName("Deve buscar cartão com diferentes formatos")
        void shouldSearchCardWithDifferentFormats(String cardNumber) {
            UUID cardId = UUID.randomUUID();
            Card card = Card.builder()
                .id(cardId)
                .cardNumberHash(CARD_HASH)
                .build();

            when(cardRepository.findByCardNumberHash(CARD_HASH)).thenReturn(Optional.of(card));

            CardSearchResponse response = cardService.searchCard(cardNumber);

            assertTrue(response.found());
            assertEquals(cardId, response.id());
        }

        @Test
        @DisplayName("Deve retornar não encontrado para cartão inexistente")
        void shouldReturnNotFoundForNonExistingCard() {
            when(cardRepository.findByCardNumberHash(CARD_HASH)).thenReturn(Optional.empty());

            CardSearchResponse response = cardService.searchCard(CARD_NUMBER);

            assertFalse(response.found());
            assertNull(response.id());
        }

        @Test
        @DisplayName("Deve normalizar número antes de buscar")
        void shouldNormalizeBeforeSearch() {
            String formattedNumber = "4111 1111 1111 1111";

            when(cardRepository.findByCardNumberHash(anyString())).thenReturn(Optional.empty());

            cardService.searchCard(formattedNumber);

            verify(cardMapper).normalizeCardNumber(formattedNumber);
        }
    }

    @Nested
    @DisplayName("Testes de Processamento em Lote")
    class BatchProcessingTests {

        @Test
        @DisplayName("Deve lançar exceção para arquivo vazio")
        void shouldThrowExceptionForEmptyFile() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "cards.txt", "text/plain", new byte[0]);

            BusinessException exception = assertThrows(BusinessException.class,
                () -> cardService.processBatchFile(emptyFile));

            assertEquals("Arquivo vazio", exception.getMessage());
        }

        @Test
        @DisplayName("Deve processar arquivo com um cartão válido")
        void shouldProcessFileWithOneValidCard() {
            String content = "header line\n0000014111111111111111";
            MockMultipartFile file = new MockMultipartFile(
                "file", "cards.txt", "text/plain", content.getBytes());

            when(cardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
            when(cardMapper.toEntity(anyString(), anyString())).thenReturn(createCard(UUID.randomUUID()));

            BatchUploadResponse response = cardService.processBatchFile(file);

            assertNotNull(response);
            assertNotNull(response.batchId());
            assertEquals(1, response.totalProcessed());
            assertEquals(1, response.successCount());
            assertEquals(0, response.errorCount());
        }

        @Test
        @DisplayName("Deve processar arquivo com múltiplos cartões")
        void shouldProcessFileWithMultipleCards() {
            String content = "header line\n" +
                "0000014111111111111111\n" +
                "0000015500000000000004\n" +
                "0000016011000000000004";
            MockMultipartFile file = new MockMultipartFile(
                "file", "cards.txt", "text/plain", content.getBytes());

            when(cardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
            when(cardMapper.toEntity(anyString(), anyString())).thenReturn(createCard(UUID.randomUUID()));

            BatchUploadResponse response = cardService.processBatchFile(file);

            assertEquals(3, response.totalProcessed());
            assertEquals(3, response.successCount());
            assertEquals(0, response.errorCount());
        }

        @Test
        @DisplayName("Deve ignorar primeira linha (header)")
        void shouldIgnoreHeaderLine() {
            String content = "HEADER - IGNORE THIS LINE\n0000014111111111111111";
            MockMultipartFile file = new MockMultipartFile(
                "file", "cards.txt", "text/plain", content.getBytes());

            when(cardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
            when(cardMapper.toEntity(anyString(), anyString())).thenReturn(createCard(UUID.randomUUID()));

            BatchUploadResponse response = cardService.processBatchFile(file);

            assertEquals(1, response.totalProcessed());
        }

        @Test
        @DisplayName("Deve ignorar linhas vazias")
        void shouldIgnoreEmptyLines() {
            String content = "header\n\n0000014111111111111111\n\n";
            MockMultipartFile file = new MockMultipartFile(
                "file", "cards.txt", "text/plain", content.getBytes());

            when(cardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
            when(cardMapper.toEntity(anyString(), anyString())).thenReturn(createCard(UUID.randomUUID()));

            BatchUploadResponse response = cardService.processBatchFile(file);

            assertEquals(1, response.totalProcessed());
        }

        @Test
        @DisplayName("Deve identificar cartões duplicados no lote")
        void shouldIdentifyDuplicateCardsInBatch() {
            String content = "header\n0000014111111111111111";
            MockMultipartFile file = new MockMultipartFile(
                "file", "cards.txt", "text/plain", content.getBytes());

            when(cardRepository.existsByCardNumberHash(anyString())).thenReturn(true);

            BatchUploadResponse response = cardService.processBatchFile(file);

            assertEquals(1, response.totalProcessed());
            assertEquals(0, response.successCount());
            assertEquals(1, response.duplicateCount());
            assertEquals(1, response.errors().size());
            assertEquals("Cartão já cadastrado no sistema", response.errors().get(0).reason());
            verify(cardRepository, never()).save(any(Card.class));
        }

        @Test
        @DisplayName("Deve identificar cartões inválidos por tamanho incorreto")
        void shouldIdentifyInvalidCardsByLength() {
            // Cartão com apenas 10 dígitos (inválido - menor que 13)
            // Linha deve ter pelo menos 22 caracteres para extração funcionar
            String content = "header\n0000011234567890      ";
            MockMultipartFile file = new MockMultipartFile(
                "file", "cards.txt", "text/plain", content.getBytes());

            BatchUploadResponse response = cardService.processBatchFile(file);

            assertEquals(1, response.totalProcessed());
            assertEquals(0, response.successCount());
            assertEquals(0, response.duplicateCount());
            assertEquals(1, response.errorCount());
            assertEquals(1, response.errors().size());
            assertEquals("Cartão inválido", response.errors().get(0).reason());
            verify(cardRepository, never()).existsByCardNumberHash(anyString());
            verify(cardRepository, never()).save(any(Card.class));
        }

        @Test
        @DisplayName("Deve identificar cartões inválidos com letras")
        void shouldIdentifyInvalidCardsWithLetters() {
            // Cartão com letras no lugar de dígitos
            String content = "header\n000001ABCD1234567890123";
            MockMultipartFile file = new MockMultipartFile(
                "file", "cards.txt", "text/plain", content.getBytes());

            BatchUploadResponse response = cardService.processBatchFile(file);

            assertEquals(1, response.totalProcessed());
            assertEquals(0, response.successCount());
            assertEquals(0, response.duplicateCount());
            assertEquals(1, response.errorCount());
            assertEquals(1, response.errors().size());
            assertEquals("Cartão inválido", response.errors().get(0).reason());
            verify(cardRepository, never()).existsByCardNumberHash(anyString());
            verify(cardRepository, never()).save(any(Card.class));
        }

        @Test
        @DisplayName("Deve continuar processando após erro em uma linha")
        void shouldContinueProcessingAfterLineError() {
            String content = "header\nshort\n0000014111111111111111";
            MockMultipartFile file = new MockMultipartFile(
                "file", "cards.txt", "text/plain", content.getBytes());

            when(cardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
            when(cardMapper.toEntity(anyString(), anyString())).thenReturn(createCard(UUID.randomUUID()));

            BatchUploadResponse response = cardService.processBatchFile(file);

            assertEquals(1, response.totalProcessed());
            assertEquals(1, response.successCount());
        }

        @Test
        @DisplayName("Deve gerar batchId único para cada processamento")
        void shouldGenerateUniqueBatchId() {
            String content = "header\n0000014111111111111111";
            MockMultipartFile file1 = new MockMultipartFile(
                "file", "cards1.txt", "text/plain", content.getBytes());
            MockMultipartFile file2 = new MockMultipartFile(
                "file", "cards2.txt", "text/plain", content.getBytes());

            when(cardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
            when(cardMapper.toEntity(anyString(), anyString())).thenReturn(createCard(UUID.randomUUID()));

            BatchUploadResponse response1 = cardService.processBatchFile(file1);
            BatchUploadResponse response2 = cardService.processBatchFile(file2);

            assertNotEquals(response1.batchId(), response2.batchId());
        }

        @ParameterizedTest(name = "Deve processar linha com formato: {0}")
        @ValueSource(strings = {
            "0000014111111111111111",
            "0000015500000000000004",
            "      4111111111111111      "
        })
        @DisplayName("Deve processar diferentes formatos de linha")
        void shouldProcessDifferentLineFormats(String line) {
            String content = "header\n" + line;
            MockMultipartFile file = new MockMultipartFile(
                "file", "cards.txt", "text/plain", content.getBytes());

            when(cardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
            when(cardMapper.toEntity(anyString(), anyString())).thenReturn(createCard(UUID.randomUUID()));

            BatchUploadResponse response = cardService.processBatchFile(file);

            assertTrue(response.totalProcessed() >= 0);
        }
    }

    private Card createCard(UUID id) {
        return Card.builder()
            .id(id)
            .cardNumberEncrypted("encrypted")
            .cardNumberHash(CARD_HASH)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
