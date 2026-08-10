package org.example.learnhub.user;

import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@DataJpaTest
public class UserRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository repository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .roleType(RoleType.USER)
                .build();

        entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldReturnUserByUsername() {
        Optional<User> result = repository.findByUsername("john");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldReturnTrueWhenUsernameExistsIgnoringCase() {
        boolean result = repository.existsByUsernameIgnoreCase("JOHN");

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotExist() {
        boolean result = repository.existsByUsernameIgnoreCase("unknown");

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenEmailExistsIgnoringCase() {
        boolean result = repository.existsByEmailIgnoreCase("JOHN@EXAMPLE.COM");

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        boolean result = repository.existsByEmailIgnoreCase("unknown@example.com");

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnUserByUsernameIgnoringCase() {
        Optional<User> result = repository.findByUsernameIgnoreCase("JOHN");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldReturnEmptyWhenUsernameDoesNotExistIgnoringCase() {
        Optional<User> result = repository.findByUsernameIgnoreCase("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnUserByEmailIgnoringCase() {
        Optional<User> result = repository.findByEmailIgnoreCase("JOHN@EXAMPLE.COM");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExistIgnoringCase() {
        Optional<User> result = repository.findByEmailIgnoreCase("unknown@example.com");

        assertThat(result).isEmpty();
    }
}