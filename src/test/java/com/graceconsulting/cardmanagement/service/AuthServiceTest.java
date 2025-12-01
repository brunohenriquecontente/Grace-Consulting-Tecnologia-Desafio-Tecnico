package com.graceconsulting.cardmanagement.service;

import com.graceconsulting.cardmanagement.dto.AuthRequest;
import com.graceconsulting.cardmanagement.dto.AuthResponse;
import com.graceconsulting.cardmanagement.security.JwtTokenProvider;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Testes de Autenticação com Sucesso")
    class SuccessfulAuthenticationTests {

        @ParameterizedTest(name = "Deve autenticar usuário: {0}")
        @CsvSource({
            "admin, admin123",
            "user, password",
            "john.doe, securePass1",
            "test_user, test_pass_123"
        })
        @DisplayName("Deve autenticar diferentes combinações de usuário/senha")
        void shouldAuthenticateDifferentCredentials(String username, String password) {
            AuthRequest request = new AuthRequest(username, password);
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
            when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt_token_" + username);
            when(jwtTokenProvider.getExpirationInSeconds()).thenReturn(3600L);

            AuthResponse response = authService.authenticate(request);

            assertNotNull(response);
            assertEquals("jwt_token_" + username, response.token());
            assertEquals("Bearer", response.type());
        }

        @ParameterizedTest(name = "Deve retornar token com expiração de {0} segundos")
        @ValueSource(longs = {3600L, 7200L, 86400L, 604800L})
        @DisplayName("Deve retornar diferentes tempos de expiração")
        void shouldReturnDifferentExpirationTimes(long expirationSeconds) {
            AuthRequest request = new AuthRequest("user", "password");
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(jwtTokenProvider.generateToken(authentication)).thenReturn("token");
            when(jwtTokenProvider.getExpirationInSeconds()).thenReturn(expirationSeconds);

            AuthResponse response = authService.authenticate(request);

            assertEquals(expirationSeconds, response.expiresIn());
        }

        @Test
        @DisplayName("Deve retornar tipo Bearer no token")
        void shouldReturnBearerTokenType() {
            AuthRequest request = new AuthRequest("user", "password");
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(jwtTokenProvider.generateToken(authentication)).thenReturn("token");
            when(jwtTokenProvider.getExpirationInSeconds()).thenReturn(3600L);

            AuthResponse response = authService.authenticate(request);

            assertEquals("Bearer", response.type());
        }
    }

    @Nested
    @DisplayName("Testes de Falha na Autenticação")
    class FailedAuthenticationTests {

        @ParameterizedTest(name = "Deve lançar exceção para senha inválida: {0}")
        @ValueSource(strings = {"wrongpassword", "123456", "invalid", ""})
        @DisplayName("Deve lançar exceção para senhas inválidas")
        void shouldThrowExceptionForInvalidPasswords(String invalidPassword) {
            AuthRequest request = new AuthRequest("testuser", invalidPassword);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

            assertThrows(BadCredentialsException.class, () -> authService.authenticate(request));
        }

        @Test
        @DisplayName("Deve propagar BadCredentialsException")
        void shouldPropagateBadCredentialsException() {
            AuthRequest request = new AuthRequest("user", "wrong");

            when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

            BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.authenticate(request));

            assertEquals("Bad credentials", exception.getMessage());
        }

        @Test
        @DisplayName("Deve propagar DisabledException para conta desabilitada")
        void shouldPropagateDisabledException() {
            AuthRequest request = new AuthRequest("disabled_user", "password");

            when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("User is disabled"));

            assertThrows(DisabledException.class, () -> authService.authenticate(request));
        }

        @Test
        @DisplayName("Deve propagar LockedException para conta bloqueada")
        void shouldPropagateLockedException() {
            AuthRequest request = new AuthRequest("locked_user", "password");

            when(authenticationManager.authenticate(any()))
                .thenThrow(new LockedException("User account is locked"));

            assertThrows(LockedException.class, () -> authService.authenticate(request));
        }

        @Test
        @DisplayName("Não deve gerar token quando autenticação falha")
        void shouldNotGenerateTokenWhenAuthenticationFails() {
            AuthRequest request = new AuthRequest("user", "wrong");

            when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid"));

            assertThrows(AuthenticationException.class, () -> authService.authenticate(request));

            verify(jwtTokenProvider, never()).generateToken(any());
        }
    }

    @Nested
    @DisplayName("Testes de Integração com AuthenticationManager")
    class AuthenticationManagerIntegrationTests {

        @Test
        @DisplayName("Deve passar credenciais corretas para AuthenticationManager")
        void shouldPassCorrectCredentialsToAuthenticationManager() {
            AuthRequest request = new AuthRequest("specificUser", "specificPass");
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(jwtTokenProvider.generateToken(any())).thenReturn("token");
            when(jwtTokenProvider.getExpirationInSeconds()).thenReturn(3600L);

            authService.authenticate(request);

            verify(authenticationManager).authenticate(
                argThat(auth ->
                    auth instanceof UsernamePasswordAuthenticationToken &&
                    auth.getPrincipal().equals("specificUser") &&
                    auth.getCredentials().equals("specificPass")
                )
            );
        }

        @Test
        @DisplayName("Deve usar Authentication retornado para gerar token")
        void shouldUseReturnedAuthenticationToGenerateToken() {
            AuthRequest request = new AuthRequest("user", "pass");
            Authentication expectedAuth = mock(Authentication.class);

            when(authenticationManager.authenticate(any())).thenReturn(expectedAuth);
            when(jwtTokenProvider.generateToken(expectedAuth)).thenReturn("token");
            when(jwtTokenProvider.getExpirationInSeconds()).thenReturn(3600L);

            authService.authenticate(request);

            verify(jwtTokenProvider).generateToken(expectedAuth);
        }

        @Test
        @DisplayName("Deve chamar autenticação antes de gerar token")
        void shouldAuthenticateBeforeGeneratingToken() {
            AuthRequest request = new AuthRequest("user", "pass");
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(jwtTokenProvider.generateToken(any())).thenReturn("token");
            when(jwtTokenProvider.getExpirationInSeconds()).thenReturn(3600L);

            authService.authenticate(request);

            var inOrder = inOrder(authenticationManager, jwtTokenProvider);
            inOrder.verify(authenticationManager).authenticate(any());
            inOrder.verify(jwtTokenProvider).generateToken(any());
        }
    }

    @Nested
    @DisplayName("Testes de Resposta de Autenticação")
    class AuthenticationResponseTests {

        @Test
        @DisplayName("Deve retornar AuthResponse completo")
        void shouldReturnCompleteAuthResponse() {
            AuthRequest request = new AuthRequest("user", "password");
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(jwtTokenProvider.generateToken(authentication)).thenReturn("generated_jwt_token");
            when(jwtTokenProvider.getExpirationInSeconds()).thenReturn(7200L);

            AuthResponse response = authService.authenticate(request);

            assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("generated_jwt_token", response.token()),
                () -> assertEquals("Bearer", response.type()),
                () -> assertEquals(7200L, response.expiresIn())
            );
        }

        @Test
        @DisplayName("Deve retornar token gerado pelo JwtTokenProvider")
        void shouldReturnTokenFromJwtTokenProvider() {
            AuthRequest request = new AuthRequest("user", "password");
            Authentication authentication = mock(Authentication.class);
            String expectedToken = "unique_jwt_token_12345";

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(jwtTokenProvider.generateToken(authentication)).thenReturn(expectedToken);
            when(jwtTokenProvider.getExpirationInSeconds()).thenReturn(3600L);

            AuthResponse response = authService.authenticate(request);

            assertEquals(expectedToken, response.token());
        }
    }
}
