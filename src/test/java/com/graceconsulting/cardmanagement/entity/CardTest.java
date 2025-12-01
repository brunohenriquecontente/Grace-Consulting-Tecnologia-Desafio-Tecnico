package com.graceconsulting.cardmanagement.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Card Entity Tests")
class CardTest {

    @Nested
    @DisplayName("Testes de Builder")
    class BuilderTests {

        @Test
        @DisplayName("Deve criar cartão com todos os campos")
        void shouldCreateCardWithAllFields() {
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            String encrypted = "encryptedCardNumber123";
            String hash = "hashValue64chars";
            String batchId = "batch-123";

            Card card = Card.builder()
                .id(id)
                .cardNumberEncrypted(encrypted)
                .cardNumberHash(hash)
                .createdAt(now)
                .batchId(batchId)
                .build();

            assertAll(
                () -> assertEquals(id, card.getId()),
                () -> assertEquals(encrypted, card.getCardNumberEncrypted()),
                () -> assertEquals(hash, card.getCardNumberHash()),
                () -> assertEquals(now, card.getCreatedAt()),
                () -> assertEquals(batchId, card.getBatchId())
            );
        }

        @Test
        @DisplayName("Deve criar cartão com createdAt padrão")
        void shouldCreateCardWithDefaultCreatedAt() {
            LocalDateTime before = LocalDateTime.now();

            Card card = Card.builder()
                .cardNumberEncrypted("encrypted")
                .cardNumberHash("hash")
                .build();

            LocalDateTime after = LocalDateTime.now();

            assertNotNull(card.getCreatedAt());
            assertTrue(card.getCreatedAt().isAfter(before.minusSeconds(1)));
            assertTrue(card.getCreatedAt().isBefore(after.plusSeconds(1)));
        }

        @Test
        @DisplayName("Deve criar cartão sem batchId")
        void shouldCreateCardWithoutBatchId() {
            Card card = Card.builder()
                .cardNumberEncrypted("encrypted")
                .cardNumberHash("hash")
                .build();

            assertNull(card.getBatchId());
        }

        @ParameterizedTest(name = "Deve criar cartão com batchId: {0}")
        @ValueSource(strings = {"batch-1", "batch-abc-123", "uuid-batch-id"})
        @DisplayName("Deve criar cartões com diferentes batchIds")
        void shouldCreateCardWithDifferentBatchIds(String batchId) {
            Card card = Card.builder()
                .cardNumberEncrypted("encrypted")
                .cardNumberHash("hash")
                .batchId(batchId)
                .build();

            assertEquals(batchId, card.getBatchId());
        }
    }

    @Nested
    @DisplayName("Testes de Getters e Setters")
    class GettersSettersTests {

        @Test
        @DisplayName("Deve permitir alterar cardNumberEncrypted")
        void shouldAllowChangingCardNumberEncrypted() {
            Card card = createDefaultCard();

            card.setCardNumberEncrypted("newEncryptedValue");

            assertEquals("newEncryptedValue", card.getCardNumberEncrypted());
        }

        @Test
        @DisplayName("Deve permitir alterar cardNumberHash")
        void shouldAllowChangingCardNumberHash() {
            Card card = createDefaultCard();

            card.setCardNumberHash("newHashValue");

            assertEquals("newHashValue", card.getCardNumberHash());
        }

        @Test
        @DisplayName("Deve permitir alterar batchId")
        void shouldAllowChangingBatchId() {
            Card card = createDefaultCard();

            card.setBatchId("new-batch-id");

            assertEquals("new-batch-id", card.getBatchId());
        }

        @ParameterizedTest(name = "Deve aceitar batchId: {0}")
        @NullAndEmptySource
        @ValueSource(strings = {"batch-1", "   ", "special-!@#"})
        @DisplayName("Deve aceitar diferentes valores para batchId")
        void shouldAcceptDifferentBatchIdValues(String batchId) {
            Card card = createDefaultCard();

            card.setBatchId(batchId);

            assertEquals(batchId, card.getBatchId());
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Cartões com todos os campos iguais devem ser iguais")
        void shouldBeEqualWhenAllFieldsAreEqual() {
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            Card card1 = Card.builder()
                .id(id)
                .cardNumberEncrypted("encrypted")
                .cardNumberHash("hash")
                .createdAt(now)
                .batchId("batch-1")
                .build();

            Card card2 = Card.builder()
                .id(id)
                .cardNumberEncrypted("encrypted")
                .cardNumberHash("hash")
                .createdAt(now)
                .batchId("batch-1")
                .build();

            assertEquals(card1, card2);
            assertEquals(card1.hashCode(), card2.hashCode());
        }

        @Test
        @DisplayName("Cartões com ids diferentes não devem ser iguais")
        void shouldNotBeEqualWhenDifferentIds() {
            LocalDateTime now = LocalDateTime.now();

            Card card1 = Card.builder()
                .id(UUID.randomUUID())
                .cardNumberEncrypted("encrypted")
                .cardNumberHash("hash")
                .createdAt(now)
                .build();

            Card card2 = Card.builder()
                .id(UUID.randomUUID())
                .cardNumberEncrypted("encrypted")
                .cardNumberHash("hash")
                .createdAt(now)
                .build();

            assertNotEquals(card1, card2);
        }

        @Test
        @DisplayName("Cartões com campos diferentes não devem ser iguais")
        void shouldNotBeEqualWhenFieldsDiffer() {
            UUID id = UUID.randomUUID();

            Card card1 = Card.builder()
                .id(id)
                .cardNumberEncrypted("encrypted1")
                .cardNumberHash("hash1")
                .build();

            Card card2 = Card.builder()
                .id(id)
                .cardNumberEncrypted("encrypted2")
                .cardNumberHash("hash2")
                .build();

            assertNotEquals(card1, card2);
        }

        @Test
        @DisplayName("Cartão não deve ser igual a null")
        void shouldNotBeEqualToNull() {
            Card card = createDefaultCard();

            assertNotEquals(null, card);
        }

        @Test
        @DisplayName("Cartão deve ser igual a si mesmo")
        void shouldBeEqualToItself() {
            Card card = createDefaultCard();

            assertEquals(card, card);
        }
    }

    @Nested
    @DisplayName("Testes de ToString")
    class ToStringTests {

        @Test
        @DisplayName("toString deve conter campos principais")
        void shouldContainMainFields() {
            UUID id = UUID.randomUUID();
            Card card = Card.builder()
                .id(id)
                .cardNumberEncrypted("encrypted")
                .cardNumberHash("hash123")
                .batchId("batch-1")
                .build();

            String toString = card.toString();

            assertAll(
                () -> assertTrue(toString.contains("Card")),
                () -> assertTrue(toString.contains(id.toString())),
                () -> assertTrue(toString.contains("encrypted")),
                () -> assertTrue(toString.contains("hash123")),
                () -> assertTrue(toString.contains("batch-1"))
            );
        }
    }

    @Nested
    @DisplayName("Testes de Construtor NoArgs")
    class NoArgsConstructorTests {

        @Test
        @DisplayName("Deve criar cartão com construtor sem argumentos")
        void shouldCreateCardWithNoArgsConstructor() {
            Card card = new Card();

            assertNotNull(card);
            assertNull(card.getId());
            assertNull(card.getCardNumberEncrypted());
            assertNull(card.getCardNumberHash());
            assertNull(card.getBatchId());
        }
    }

    @Nested
    @DisplayName("Testes de Construtor AllArgs")
    class AllArgsConstructorTests {

        @Test
        @DisplayName("Deve criar cartão com construtor com todos os argumentos")
        void shouldCreateCardWithAllArgsConstructor() {
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            Card card = new Card(id, "encrypted", "hash", now, "batch-1");

            assertAll(
                () -> assertEquals(id, card.getId()),
                () -> assertEquals("encrypted", card.getCardNumberEncrypted()),
                () -> assertEquals("hash", card.getCardNumberHash()),
                () -> assertEquals(now, card.getCreatedAt()),
                () -> assertEquals("batch-1", card.getBatchId())
            );
        }
    }

    private Card createDefaultCard() {
        return Card.builder()
            .id(UUID.randomUUID())
            .cardNumberEncrypted("encryptedData")
            .cardNumberHash("hashValue")
            .batchId("batch-123")
            .build();
    }
}
