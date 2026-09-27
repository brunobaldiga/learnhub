package org.example.learnhub.enrollment;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.enrollment.repository.EnrollmentRepository;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
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
class EnrollmentRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired TestEntityManager entityManager;
    @Autowired EnrollmentRepository repository;

    private User user;
    private User otherUser;
    private Course course;

    @BeforeEach
    void setUp() {
        user = entityManager.persist(User.builder()
                .username("john").email("john@example.com").fullName("John Doe")
                .password("password").roleType(RoleType.USER).build());
        otherUser = entityManager.persist(User.builder()
                .username("mary").email("mary@example.com").fullName("Mary Doe")
                .password("password").roleType(RoleType.USER).build());
        course = entityManager.persist(Course.builder()
                .creatorId(otherUser.getId()).title("Java Course").status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN).currency(CurrencyCode.USD).build());
        entityManager.flush();
        entityManager.clear();
    }

    private Enrollment persistEnrollment() {
        Enrollment enrollment = entityManager.persist(Enrollment.builder()
                .userId(user.getId()).courseId(course.getId()).build());
        entityManager.flush();
        entityManager.clear();
        return enrollment;
    }

    @Test
    void shouldFindEnrollmentsByUserId() {
        persistEnrollment();
        assertThat(repository.findByUserId(user.getId(), PageRequest.of(0, 10)).getContent()).hasSize(1);
    }

    @Test
    void shouldFindEnrollmentByIdAndUserId() {
        Enrollment enrollment = persistEnrollment();
        assertThat(repository.findByIdAndUserId(enrollment.getId(), user.getId())).isPresent();
        assertThat(repository.findByIdAndUserId(enrollment.getId(), otherUser.getId())).isEmpty();
    }

    @Test
    void shouldFindEnrollmentByUserAndCourse() {
        Enrollment enrollment = persistEnrollment();
        assertThat(repository.findByUserIdAndCourseId(user.getId(), course.getId()))
                .isPresent()
                .get()
                .extracting(Enrollment::getId)
                .isEqualTo(enrollment.getId());
    }

    @Test
    void shouldCheckEnrollmentExistence() {
        assertThat(repository.existsByUserIdAndCourseId(user.getId(), course.getId())).isFalse();
        persistEnrollment();
        assertThat(repository.existsByUserIdAndCourseId(user.getId(), course.getId())).isTrue();
    }
}
