package com.graceconsulting.cardmanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EncryptionService Tests")
class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService();
        ReflectionTestUtils.setField(encryptionService, "secretKey",
            "test-secret-key-for-jwt-minimum-256-bits-required-here");
        encryptionService.init();
    }

    @Nested
    @DisplayName("Testes de Criptografia")
    class EncryptionTests {

        @ParameterizedTest(name = "Deve criptografar e descriptografar: {0}")
        @ValueSource(strings = {
            "4111111111111111",
            "5500000000000004",
            "340000000000009",
            "30000000000004",
            "6011000000000004",
            "3528000000000007"
        })
        @DisplayName("Deve criptografar e descriptografar diferentes números de cartão")
        void shouldEncryptAndDecryptDifferentCardNumbers(String cardNumber) {
            String encrypted = encryptionService.encrypt(cardNumber);
            String decrypted = encryptionService.decrypt(encrypted);

            assertNotNull(encrypted);
            assertNotEquals(cardNumber, encrypted);
            assertEquals(cardNumber, decrypted);
        }

        @ParameterizedTest(name = "Deve criptografar dados com caracteres especiais: {0}")
        @ValueSource(strings = {
            "test@123",
            "data with spaces",
            "special!@#$%",
            "números123",
            "日本語テスト"
        })
        @DisplayName("Deve criptografar dados com diferentes caracteres")
        void shouldEncryptDataWithSpecialCharacters(String data) {
            String encrypted = encryptionService.encrypt(data);
            String decrypted = encryptionService.decrypt(encrypted);

            assertEquals(data, decrypted);
        }

        @Test
        @DisplayName("Deve gerar criptografias diferentes para o mesmo dado (IV aleatório)")
        void shouldGenerateDifferentEncryptionsForSameData() {
            String data = "4111111111111111";

            String encrypted1 = encryptionService.encrypt(data);
            String encrypted2 = encryptionService.encrypt(data);
            String encrypted3 = encryptionService.encrypt(data);

            assertNotEquals(encrypted1, encrypted2);
            assertNotEquals(encrypted2, encrypted3);
            assertNotEquals(encrypted1, encrypted3);
        }

        @Test
        @DisplayName("Deve descriptografar corretamente após múltiplas criptografias")
        void shouldDecryptCorrectlyAfterMultipleEncryptions() {
            String original = "4111111111111111";

            for (int i = 0; i < 10; i++) {
                String encrypted = encryptionService.encrypt(original);
                String decrypted = encryptionService.decrypt(encrypted);
                assertEquals(original, decrypted, "Falha na iteração " + i);
            }
        }
    }

    @Nested
    @DisplayName("Testes de Hash")
    class HashTests {

        @ParameterizedTest(name = "Deve gerar hash consistente para: {0}")
        @ValueSource(strings = {
            "4111111111111111",
            "5500000000000004",
            "340000000000009",
            "test-data",
            "another-test"
        })
        @DisplayName("Deve gerar hash consistente para diferentes entradas")
        void shouldGenerateConsistentHashForDifferentInputs(String input) {
            String hash1 = encryptionService.hash(input);
            String hash2 = encryptionService.hash(input);
            String hash3 = encryptionService.hash(input);

            assertEquals(hash1, hash2);
            assertEquals(hash2, hash3);
            assertEquals(64, hash1.length(), "Hash SHA-256 deve ter 64 caracteres hexadecimais");
        }

        @ParameterizedTest(name = "Deve gerar hashes diferentes para {0} e {1}")
        @CsvSource({
            "4111111111111111, 4222222222222222",
            "test1, test2",
            "abc, ABC",
            "data1, data2",
            "123, 1234"
        })
        @DisplayName("Deve gerar hashes diferentes para dados diferentes")
        void shouldGenerateDifferentHashesForDifferentData(String data1, String data2) {
            String hash1 = encryptionService.hash(data1);
            String hash2 = encryptionService.hash(data2);

            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("Deve gerar hashes diferentes para dados com espaços")
        void shouldGenerateDifferentHashesForDataWithSpaces() {
            String hash1 = encryptionService.hash("data");
            String hash2 = encryptionService.hash("data ");

            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("Hash deve conter apenas caracteres hexadecimais")
        void shouldContainOnlyHexCharacters() {
            String hash = encryptionService.hash("test-data");

            assertTrue(hash.matches("[0-9a-f]+"), "Hash deve conter apenas caracteres hexadecimais minúsculos");
        }
    }

    @Nested
    @DisplayName("Testes de Mascaramento")
    class MaskTests {

        @ParameterizedTest(name = "Deve mascarar {0} como {1}")
        @CsvSource({
            "4111111111111111, 4111****1111",
            "5500000000000004, 5500****0004",
            "340000000000009, 3400****0009",
            "6011000000000004, 6011****0004",
            "12345678, 1234****5678"
        })
        @DisplayName("Deve mascarar corretamente números de cartão válidos")
        void shouldMaskValidCardNumbers(String cardNumber, String expectedMask) {
            String masked = encryptionService.maskCardNumber(cardNumber);

            assertEquals(expectedMask, masked);
        }

        @ParameterizedTest(name = "Deve retornar **** para entrada curta: {0}")
        @ValueSource(strings = {"1", "12", "123", "1234", "12345", "123456", "1234567"})
        @DisplayName("Deve retornar **** para números curtos (menos de 8 caracteres)")
        void shouldReturnAsterisksForShortNumbers(String shortNumber) {
            String masked = encryptionService.maskCardNumber(shortNumber);

            assertEquals("****", masked);
        }

        @ParameterizedTest(name = "Deve retornar **** para entrada nula ou vazia")
        @NullAndEmptySource
        @DisplayName("Deve retornar **** para entrada nula ou vazia")
        void shouldReturnAsterisksForNullOrEmpty(String input) {
            String masked = encryptionService.maskCardNumber(input);

            assertEquals("****", masked);
        }

        @Test
        @DisplayName("Deve mascarar cartão com exatamente 8 caracteres")
        void shouldMaskCardWithExactly8Characters() {
            String cardNumber = "12345678";

            String masked = encryptionService.maskCardNumber(cardNumber);

            assertEquals("1234****5678", masked);
        }
    }

    @Nested
    @DisplayName("Testes de Inicialização")
    class InitializationTests {

        @Test
        @DisplayName("Deve inicializar corretamente com chave válida")
        void shouldInitializeWithValidKey() {
            EncryptionService service = new EncryptionService();
            ReflectionTestUtils.setField(service, "secretKey", "valid-secret-key-minimum-length");

            assertDoesNotThrow(service::init);
        }

        @Test
        @DisplayName("Deve funcionar após reinicialização")
        void shouldWorkAfterReinitialization() {
            String data = "test-data";
            String encrypted1 = encryptionService.encrypt(data);

            // Reinicializa
            encryptionService.init();

            String decrypted = encryptionService.decrypt(encrypted1);
            assertEquals(data, decrypted);
        }
    }
}
