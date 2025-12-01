package com.graceconsulting.cardmanagement.mapper;

import com.graceconsulting.cardmanagement.dto.UserRegisterRequest;
import com.graceconsulting.cardmanagement.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserMapper Tests")
class UserMapperTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
        ReflectionTestUtils.setField(userMapper, "passwordEncoder", passwordEncoder);
    }

    @Test
    @DisplayName("Deve converter UserRegisterRequest para User")
    void shouldConvertUserRegisterRequestToUser() {
        UserRegisterRequest request = new UserRegisterRequest("testuser", "password123", "Test User");

        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");

        User result = userMapper.toEntity(request);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encoded_password", result.getPassword());
        assertEquals("Test User", result.getName());
    }

    @Test
    @DisplayName("Deve codificar senha ao converter")
    void shouldEncodePasswordWhenConverting() {
        UserRegisterRequest request = new UserRegisterRequest("user", "mypass", "User Name");

        when(passwordEncoder.encode("mypass")).thenReturn("encoded_mypass");

        User result = userMapper.toEntity(request);

        assertEquals("encoded_mypass", result.getPassword());
    }

    @Test
    @DisplayName("User convertido deve estar ativo por padrão")
    void shouldBeActiveByDefault() {
        UserRegisterRequest request = new UserRegisterRequest("user", "pass", "Name");

        when(passwordEncoder.encode("pass")).thenReturn("encoded");

        User result = userMapper.toEntity(request);

        assertTrue(result.isActive());
    }
}
