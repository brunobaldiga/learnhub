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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {
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
        user = entityManager.persist(User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .roleType(RoleType.USER)
                .build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldReturnUserByUsername() {
        Optional<User> result = repository.findByUsername("john");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldReturnTrueWhenUsernameExistsIgnoringCase() {
        assertThat(repository.existsByUsernameIgnoreCase("JOHN")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotExist() {
        assertThat(repository.existsByUsernameIgnoreCase("unknown")).isFalse();
    }

    @Test
    void shouldReturnTrueWhenEmailExistsIgnoringCase() {
        assertThat(repository.existsByEmailIgnoreCase("JOHN@EXAMPLE.COM")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        assertThat(repository.existsByEmailIgnoreCase("unknown@example.com")).isFalse();
    }

    @Test
    void shouldReturnUserByUsernameIgnoringCase() {
        Optional<User> result = repository.findByUsernameIgnoreCase("JOHN");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldReturnEmptyWhenUsernameDoesNotExistIgnoringCase() {
        assertThat(repository.findByUsernameIgnoreCase("unknown")).isEmpty();
    }

    @Test
    void shouldReturnUserByEmailIgnoringCase() {
        Optional<User> result = repository.findByEmailIgnoreCase("JOHN@EXAMPLE.COM");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExistIgnoringCase() {
        assertThat(repository.findByEmailIgnoreCase("unknown@example.com")).isEmpty();
    }

    @Test
    void shouldFindIdsByUsernameContainingIgnoringCase() {
        User second = entityManager.persist(User.builder()
                .username("JohnnyBravo")
                .email("johnny@example.com")
                .fullName("Johnny Bravo")
                .password("password")
                .roleType(RoleType.USER)
                .build());
        entityManager.flush();
        entityManager.clear();

        List<Integer> result = repository.findIdsByUsernameContaining("JOHN");

        assertThat(result).containsExactlyInAnyOrder(user.getId(), second.getId());
    }
}
