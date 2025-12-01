package com.graceconsulting.cardmanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
            "test-secret-key-for-jwt-minimum-256-bits-required-here");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 3600000L);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("Deve gerar token válido")
    void shouldGenerateValidToken() {
        Authentication authentication = createAuthentication("testuser");

        String token = jwtTokenProvider.generateToken(authentication);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    @DisplayName("Deve extrair username do token")
    void shouldExtractUsernameFromToken() {
        Authentication authentication = createAuthentication("testuser");
        String token = jwtTokenProvider.generateToken(authentication);

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Deve validar token válido")
    void shouldValidateValidToken() {
        Authentication authentication = createAuthentication("testuser");
        String token = jwtTokenProvider.generateToken(authentication);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Deve invalidar token malformado")
    void shouldInvalidateMalformedToken() {
        boolean isValid = jwtTokenProvider.validateToken("invalid.token.here");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve invalidar token nulo")
    void shouldInvalidateNullToken() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve invalidar token vazio")
    void shouldInvalidateEmptyToken() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve retornar expiração em segundos")
    void shouldReturnExpirationInSeconds() {
        long expiration = jwtTokenProvider.getExpirationInSeconds();

        assertEquals(3600L, expiration);
    }

    @Test
    @DisplayName("Token de usuários diferentes devem ser diferentes")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        Authentication auth1 = createAuthentication("user1");
        Authentication auth2 = createAuthentication("user2");

        String token1 = jwtTokenProvider.generateToken(auth1);
        String token2 = jwtTokenProvider.generateToken(auth2);

        assertNotEquals(token1, token2);
    }

    private Authentication createAuthentication(String username) {
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
