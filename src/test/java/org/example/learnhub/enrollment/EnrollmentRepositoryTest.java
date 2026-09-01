package org.example.learnhub.enrollment;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.enrollment.repository.CertificateRepository;
import org.example.learnhub.enrollment.repository.EnrollmentRepository;
import org.example.learnhub.enrollment.repository.LessonProgressRepository;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
public class  EnrollmentRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EnrollmentRepository repository;

    private User user;
    private Course course;

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

        course = Course.builder()
                .creatorId(user.getId())
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .averageRating(0.0)
                .build();

        entityManager.persist(course);
        entityManager.flush();
        entityManager.clear();
    }


    @Test
    void shouldReturnEnrollmentsByUserIdSuccessfully() {
        Enrollment enrollment = Enrollment.builder()
                .userId(user.getId())
                .courseId(course.getId())
                .build();

        entityManager.persist(enrollment);
        entityManager.flush();
        entityManager.clear();

        Page<Enrollment> result = repository.findByUserId(user.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCourseId()).isEqualTo(course.getId());
    }

    @Test
    void shouldReturnEmptyWhenUserHasNoEnrollments() {
        Page<Enrollment> result = repository.findByUserId(user.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldReturnEnrollmentByIdAndUserIdSuccessfully() {
        Enrollment enrollment = entityManager.persist(Enrollment.builder()
                .userId(user.getId())
                .courseId(course.getId())
                .build());

        entityManager.flush();
        entityManager.clear();

        Optional<Enrollment> result = repository.findByIdAndUserId(enrollment.getId(), user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(enrollment.getId());
        assertThat(result.get().getCourseId()).isEqualTo(course.getId());
    }

    @Test
    void shouldReturnEmptyWhenEnrollmentBelongsToAnotherUser() {
        Enrollment enrollment = entityManager.persist(Enrollment.builder()
                .userId(user.getId())
                .courseId(course.getId())
                .build());

        User otherUser = User.builder()
                .username("mary")
                .email("mary@example.com")
                .fullName("Mary Doe")
                .password("password")
                .roleType(RoleType.USER)
                .build();

        entityManager.persist(otherUser);
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findByIdAndUserId(enrollment.getId(), otherUser.getId())).isEmpty();
    }

    @Test
    void shouldReturnEnrollmentByUserIdAndCourseIdSuccessfully() {
        Enrollment enrollment = entityManager.persist(Enrollment.builder()
                .userId(user.getId())
                .courseId(course.getId())
                .build());

        entityManager.flush();
        entityManager.clear();

        Optional<Enrollment> result = repository.findByUserIdAndCourseId(user.getId(), course.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(enrollment.getId());
    }

    @Test
    void shouldReturnEmptyWhenUserIsNotEnrolledInCourse() {
        assertThat(repository.findByUserIdAndCourseId(user.getId(), course.getId())).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenEnrollmentExists() {
        entityManager.persist(Enrollment.builder()
                .userId(user.getId())
                .courseId(course.getId())
                .build());

        entityManager.flush();

        assertThat(repository.existsByUserIdAndCourseId(user.getId(), course.getId())).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEnrollmentDoesNotExist() {
        assertThat(repository.existsByUserIdAndCourseId(user.getId(), course.getId())).isFalse();
    }
}