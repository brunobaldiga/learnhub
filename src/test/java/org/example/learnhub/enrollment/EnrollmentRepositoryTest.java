package org.example.learnhub.enrollment;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.enrollment.repository.EnrollmentRepository;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
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
public class EnrollmentRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private EnrollmentRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindEnrollmentByUserId() {
        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .build();

        User savedUser = entityManager.persist(user);

        Course course1 = Course.builder()
                .creatorId(savedUser.getId())
                .title("Java Course")
                .averageRating(0.0)
                .build();

        Course course2 = Course.builder()
                .creatorId(savedUser.getId())
                .title("Java Course")
                .averageRating(0.0)
                .build();


        Course savedCourse1 = entityManager.persist(course1);
        Course savedCourse2 = entityManager.persist(course2);

        entityManager.persist(Enrollment.builder().userId(savedUser.getId()).courseId(savedCourse1.getId()).build());
        entityManager.persist(Enrollment.builder().userId(savedUser.getId()).courseId(savedCourse2.getId()).build());

        entityManager.flush();
        entityManager.clear();

        Page<Enrollment> result = repository.findByUserId(savedUser.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Enrollment::getCourseId)
                .containsExactlyInAnyOrder(savedCourse1.getId(), savedCourse2.getId());
    }

    @Test
    void shouldFindEnrollmentByIdAndUserId() {
        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .roleType(RoleType.USER)
                .build();

        User savedUser = entityManager.persist(user);

        Course course = Course.builder()
                .creatorId(savedUser.getId())
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .averageRating(0.0)
                .build();

        Course savedCourse = entityManager.persist(course);

        Enrollment enrollment = Enrollment.builder()
                .userId(savedUser.getId())
                .courseId(savedCourse.getId())
                .build();

        Enrollment savedEnrollment = entityManager.persist(enrollment);

        entityManager.flush();
        entityManager.clear();

        Optional<Enrollment> result = repository.findByCourseIdAndUserId(savedCourse.getId(), savedUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getCourseId()).isEqualTo(savedCourse.getId());
        assertThat(result.get().getUserId()).isEqualTo(savedUser.getId());
    }

    @Test
    void shouldReturnEmptyWhenNoEnrollmentExists() {
        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .roleType(RoleType.USER)
                .build();

        User savedUser = entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();

        Optional<Enrollment> result = repository.findByCourseIdAndUserId(999, savedUser.getId());

        assertThat(result).isEmpty();
    }
}