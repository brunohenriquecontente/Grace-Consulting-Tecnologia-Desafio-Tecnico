package com.graceconsulting.cardmanagement.service;

import com.graceconsulting.cardmanagement.dto.UserRegisterRequest;
import com.graceconsulting.cardmanagement.entity.User;
import com.graceconsulting.cardmanagement.exception.ResourceConflictException;
import com.graceconsulting.cardmanagement.mapper.UserMapper;
import com.graceconsulting.cardmanagement.repository.UserRepository;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("Testes de Carregamento de Usuário")
    class LoadUserTests {

        @ParameterizedTest(name = "Deve carregar usuário: {0}")
        @ValueSource(strings = {"admin", "user", "testuser", "john.doe", "user_123"})
        @DisplayName("Deve carregar diferentes usernames válidos")
        void shouldLoadDifferentValidUsernames(String username) {
            User user = createUser(username, "Test User");

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

            UserDetails result = userService.loadUserByUsername(username);

            assertNotNull(result);
            assertEquals(username, result.getUsername());
        }

        @ParameterizedTest(name = "Deve lançar exceção para usuário inexistente: {0}")
        @ValueSource(strings = {"nonexistent", "unknown", "deleted_user"})
        @DisplayName("Deve lançar exceção para usuários não encontrados")
        void shouldThrowExceptionForNonExistentUsers(String username) {
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(username));

            assertTrue(exception.getMessage().contains(username));
        }

        @Test
        @DisplayName("Deve retornar UserDetails com password correto")
        void shouldReturnUserDetailsWithCorrectPassword() {
            String expectedPassword = "encodedPassword123";
            User user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .password(expectedPassword)
                .name("Test User")
                .active(true)
                .build();

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

            UserDetails result = userService.loadUserByUsername("testuser");

            assertEquals(expectedPassword, result.getPassword());
        }
    }

    @Nested
    @DisplayName("Testes de Registro de Usuário")
    class RegisterUserTests {

        @ParameterizedTest(name = "Deve registrar usuário: {0}")
        @CsvSource({
            "newuser, password123, New User",
            "admin, admin123, Administrator",
            "john.doe, securePass1, John Doe",
            "user_123, pass_456, User 123"
        })
        @DisplayName("Deve registrar usuários com diferentes dados")
        void shouldRegisterUsersWithDifferentData(String username, String password, String name) {
            UserRegisterRequest request = new UserRegisterRequest(username, password, name);
            User userToSave = createUser(username, name);
            User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .password("encoded_" + password)
                .name(name)
                .active(true)
                .build();

            when(userRepository.existsByUsername(username)).thenReturn(false);
            when(userMapper.toEntity(request)).thenReturn(userToSave);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            User result = userService.register(request);

            assertNotNull(result);
            assertEquals(username, result.getUsername());
            assertEquals(name, result.getName());
            verify(userRepository).save(any(User.class));
        }

        @ParameterizedTest(name = "Deve lançar exceção para username duplicado: {0}")
        @ValueSource(strings = {"existinguser", "admin", "duplicated"})
        @DisplayName("Deve lançar ResourceConflictException para usernames já existentes")
        void shouldThrowExceptionForDuplicateUsernames(String username) {
            UserRegisterRequest request = new UserRegisterRequest(username, "password123", "User");

            when(userRepository.existsByUsername(username)).thenReturn(true);

            ResourceConflictException exception = assertThrows(ResourceConflictException.class,
                () -> userService.register(request));

            assertEquals("Username já está em uso", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
            verify(userMapper, never()).toEntity(any(UserRegisterRequest.class));
        }

        @Test
        @DisplayName("Deve chamar mapper antes de salvar")
        void shouldCallMapperBeforeSaving() {
            UserRegisterRequest request = new UserRegisterRequest("user", "password", "User");
            User userToSave = createUser("user", "User");
            User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .password("encoded")
                .name("User")
                .build();

            when(userRepository.existsByUsername("user")).thenReturn(false);
            when(userMapper.toEntity(request)).thenReturn(userToSave);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            userService.register(request);

            var inOrder = inOrder(userMapper, userRepository);
            inOrder.verify(userMapper).toEntity(request);
            inOrder.verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Deve verificar duplicidade antes de converter")
        void shouldCheckDuplicateBeforeMapping() {
            UserRegisterRequest request = new UserRegisterRequest("existing", "password", "User");

            when(userRepository.existsByUsername("existing")).thenReturn(true);

            assertThrows(ResourceConflictException.class, () -> userService.register(request));

            var inOrder = inOrder(userRepository, userMapper);
            inOrder.verify(userRepository).existsByUsername("existing");
            inOrder.verify(userMapper, never()).toEntity(any());
        }

        @Test
        @DisplayName("Deve retornar usuário salvo")
        void shouldReturnSavedUser() {
            UUID expectedId = UUID.randomUUID();
            UserRegisterRequest request = new UserRegisterRequest("newuser", "password", "New User");
            User userToSave = createUser("newuser", "New User");
            User savedUser = User.builder()
                .id(expectedId)
                .username("newuser")
                .password("encoded")
                .name("New User")
                .active(true)
                .build();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userMapper.toEntity(request)).thenReturn(userToSave);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            User result = userService.register(request);

            assertEquals(expectedId, result.getId());
            assertEquals("newuser", result.getUsername());
            assertEquals("New User", result.getName());
        }
    }

    private User createUser(String username, String name) {
        return User.builder()
            .id(UUID.randomUUID())
            .username(username)
            .password("encodedPassword")
            .name(name)
            .active(true)
            .build();
    }
}
