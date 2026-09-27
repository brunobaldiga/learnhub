package org.example.learnhub.course;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseReview;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.repository.CourseReviewRepository;
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
class CourseReviewRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    TestEntityManager entityManager;
    @Autowired
    CourseReviewRepository repository;

    private User student;
    private Course course;
    private CourseReview review;

    @BeforeEach
    void setUp() {
        User creator = entityManager.persist(User.builder()
                .username("creator")
                .email("creator@example.com")
                .fullName("Creator One")
                .password("password")
                .roleType(RoleType.CREATOR)
                .build());
        student = entityManager.persist(User.builder()
                .username("student")
                .email("student@example.com")
                .fullName("Student One")
                .password("password")
                .roleType(RoleType.USER)
                .build());
        course = entityManager.persist(Course.builder()
                .creatorId(creator.getId())
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .currency(CurrencyCode.USD)
                .build());
        review = entityManager.persist(CourseReview.builder()
                .course(course)
                .authorId(student.getId())
                .rating(5)
                .comment("Excellent course")
                .build());
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindReviewByIdAndCourseId() {
        assertThat(repository.findByIdAndCourseId(review.getId(), course.getId())).isPresent();
    }

    @Test
    void shouldReturnEmptyForWrongCourseId() {
        assertThat(repository.findByIdAndCourseId(review.getId(), course.getId() + 100)).isEmpty();
    }

    @Test
    void shouldCheckWhetherAuthorAlreadyReviewedCourse() {
        assertThat(repository.existsByAuthorIdAndCourseId(student.getId(), course.getId())).isTrue();
        assertThat(repository.existsByAuthorIdAndCourseId(student.getId() + 100, course.getId())).isFalse();
    }
}
