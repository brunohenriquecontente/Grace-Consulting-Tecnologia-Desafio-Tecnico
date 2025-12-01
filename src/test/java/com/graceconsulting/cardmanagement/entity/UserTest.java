package com.graceconsulting.cardmanagement.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    @Nested
    @DisplayName("Testes de Builder")
    class BuilderTests {

        @Test
        @DisplayName("Deve criar usuário com todos os campos")
        void shouldCreateUserWithAllFields() {
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            User user = User.builder()
                .id(id)
                .username("testuser")
                .password("encodedPassword")
                .name("Test User")
                .active(true)
                .createdAt(now)
                .build();

            assertAll(
                () -> assertEquals(id, user.getId()),
                () -> assertEquals("testuser", user.getUsername()),
                () -> assertEquals("encodedPassword", user.getPassword()),
                () -> assertEquals("Test User", user.getName()),
                () -> assertTrue(user.isActive()),
                () -> assertEquals(now, user.getCreatedAt())
            );
        }

        @Test
        @DisplayName("Deve criar usuário com valores padrão")
        void shouldCreateUserWithDefaultValues() {
            User user = User.builder()
                .username("testuser")
                .password("password")
                .name("Test User")
                .build();

            assertAll(
                () -> assertTrue(user.isActive(), "active deve ser true por padrão"),
                () -> assertNotNull(user.getCreatedAt(), "createdAt deve ser preenchido automaticamente")
            );
        }

        @ParameterizedTest(name = "Deve criar usuário com username: {0}")
        @ValueSource(strings = {"admin", "user", "john.doe", "user_123", "test@user"})
        @DisplayName("Deve criar usuários com diferentes usernames")
        void shouldCreateUserWithDifferentUsernames(String username) {
            User user = User.builder()
                .username(username)
                .password("password")
                .name("User")
                .build();

            assertEquals(username, user.getUsername());
        }
    }

    @Nested
    @DisplayName("Testes de UserDetails")
    class UserDetailsTests {

        @Test
        @DisplayName("Deve retornar autoridades com ROLE_USER")
        void shouldReturnAuthoritiesWithRoleUser() {
            User user = createDefaultUser();

            var authorities = user.getAuthorities();

            assertNotNull(authorities);
            assertEquals(1, authorities.size());
            assertTrue(authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_USER")));
        }

        @Test
        @DisplayName("isAccountNonExpired deve retornar true")
        void shouldReturnAccountNonExpiredAsTrue() {
            User user = createDefaultUser();

            assertTrue(user.isAccountNonExpired());
        }

        @Test
        @DisplayName("isAccountNonLocked deve retornar true")
        void shouldReturnAccountNonLockedAsTrue() {
            User user = createDefaultUser();

            assertTrue(user.isAccountNonLocked());
        }

        @Test
        @DisplayName("isCredentialsNonExpired deve retornar true")
        void shouldReturnCredentialsNonExpiredAsTrue() {
            User user = createDefaultUser();

            assertTrue(user.isCredentialsNonExpired());
        }

        @Test
        @DisplayName("isEnabled deve retornar valor de active")
        void shouldReturnEnabledBasedOnActive() {
            User activeUser = User.builder()
                .username("active")
                .password("pass")
                .name("Active User")
                .active(true)
                .build();

            User inactiveUser = User.builder()
                .username("inactive")
                .password("pass")
                .name("Inactive User")
                .active(false)
                .build();

            assertTrue(activeUser.isEnabled());
            assertFalse(inactiveUser.isEnabled());
        }

        @ParameterizedTest(name = "isEnabled deve ser {0} quando active é {0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("isEnabled deve refletir o valor de active")
        void shouldReflectActiveInEnabled(boolean activeValue) {
            User user = User.builder()
                .username("user")
                .password("pass")
                .name("User")
                .active(activeValue)
                .build();

            assertEquals(activeValue, user.isEnabled());
        }
    }

    @Nested
    @DisplayName("Testes de Getters e Setters")
    class GettersSettersTests {

        @Test
        @DisplayName("Deve permitir alterar username")
        void shouldAllowChangingUsername() {
            User user = createDefaultUser();

            user.setUsername("newusername");

            assertEquals("newusername", user.getUsername());
        }

        @Test
        @DisplayName("Deve permitir alterar password")
        void shouldAllowChangingPassword() {
            User user = createDefaultUser();

            user.setPassword("newpassword");

            assertEquals("newpassword", user.getPassword());
        }

        @Test
        @DisplayName("Deve permitir alterar name")
        void shouldAllowChangingName() {
            User user = createDefaultUser();

            user.setName("New Name");

            assertEquals("New Name", user.getName());
        }

        @Test
        @DisplayName("Deve permitir alterar active")
        void shouldAllowChangingActive() {
            User user = createDefaultUser();

            user.setActive(false);

            assertFalse(user.isActive());
            assertFalse(user.isEnabled());
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Usuários com todos os campos iguais devem ser iguais")
        void shouldBeEqualWhenAllFieldsAreEqual() {
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            User user1 = User.builder()
                .id(id)
                .username("user")
                .password("pass")
                .name("User")
                .active(true)
                .createdAt(now)
                .build();

            User user2 = User.builder()
                .id(id)
                .username("user")
                .password("pass")
                .name("User")
                .active(true)
                .createdAt(now)
                .build();

            assertEquals(user1, user2);
            assertEquals(user1.hashCode(), user2.hashCode());
        }

        @Test
        @DisplayName("Usuários com ids diferentes não devem ser iguais")
        void shouldNotBeEqualWhenDifferentIds() {
            LocalDateTime now = LocalDateTime.now();

            User user1 = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .password("pass")
                .name("User")
                .createdAt(now)
                .build();

            User user2 = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .password("pass")
                .name("User")
                .createdAt(now)
                .build();

            assertNotEquals(user1, user2);
        }

        @Test
        @DisplayName("Usuários com campos diferentes não devem ser iguais")
        void shouldNotBeEqualWhenFieldsDiffer() {
            UUID id = UUID.randomUUID();

            User user1 = User.builder()
                .id(id)
                .username("user1")
                .password("pass1")
                .name("User 1")
                .build();

            User user2 = User.builder()
                .id(id)
                .username("user2")
                .password("pass2")
                .name("User 2")
                .build();

            assertNotEquals(user1, user2);
        }

        @Test
        @DisplayName("Usuário deve ser igual a si mesmo")
        void shouldBeEqualToItself() {
            User user = createDefaultUser();

            assertEquals(user, user);
        }

        @Test
        @DisplayName("Usuário não deve ser igual a null")
        void shouldNotBeEqualToNull() {
            User user = createDefaultUser();

            assertNotEquals(null, user);
        }
    }

    private User createDefaultUser() {
        return User.builder()
            .id(UUID.randomUUID())
            .username("testuser")
            .password("encodedPassword")
            .name("Test User")
            .active(true)
            .build();
    }
}
