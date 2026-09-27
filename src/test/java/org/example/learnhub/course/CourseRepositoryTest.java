package org.example.learnhub.course;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.repository.CourseRepository;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CourseRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    TestEntityManager entityManager;
    @Autowired
    CourseRepository repository;

    private User creator;
    private User otherCreator;
    private Course course;

    @BeforeEach
    void setUp() {
        creator = entityManager.persist(User.builder()
                .username("creator")
                .email("creator@example.com")
                .fullName("Creator One")
                .password("password")
                .roleType(RoleType.CREATOR)
                .build());

        otherCreator = entityManager.persist(User.builder()
                .username("other")
                .email("other@example.com")
                .fullName("Creator Two")
                .password("password")
                .roleType(RoleType.CREATOR)
                .build());

        course = entityManager.persist(Course.builder()
                .creatorId(creator.getId())
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .currency(CurrencyCode.USD)
                .build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindCourseByIdAndStatus() {
        assertThat(repository.findByIdAndStatus(course.getId(), CourseStatus.PUBLIC))
                .isPresent()
                .get()
                .extracting(Course::getId)
                .isEqualTo(course.getId());
    }

    @Test
    void shouldReturnEmptyWhenStatusDoesNotMatch() {
        assertThat(repository.findByIdAndStatus(course.getId(), CourseStatus.PRIVATE)).isEmpty();
    }

    @Test
    void shouldFindCourseByIdAndCreatorId() {
        assertThat(repository.findByIdAndCreatorId(course.getId(), creator.getId())).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenCourseBelongsToAnotherCreator() {
        assertThat(repository.findByIdAndCreatorId(course.getId(), otherCreator.getId())).isEmpty();
    }

    @Test
    void shouldCheckCourseOwnership() {
        assertThat(repository.existsByIdAndCreatorId(course.getId(), creator.getId())).isTrue();
        assertThat(repository.existsByIdAndCreatorId(course.getId(), otherCreator.getId())).isFalse();
    }
}
