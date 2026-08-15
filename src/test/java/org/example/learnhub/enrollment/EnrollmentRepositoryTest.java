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
    void shouldFindEnrollmentByUserAndCourse() {
        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .build();

        Course course = Course.builder()
                .creator(user)
                .title("Java Course")
                .averageRating(0.0)
                .build();

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .build();

        entityManager.persist(user);
        entityManager.persist(course);
        entityManager.persist(enrollment);
        entityManager.flush();
        entityManager.clear();

        Optional<Enrollment> result = repository.findByUserAndCourse(user, course);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(enrollment.getId());
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().getCourse().getId()).isEqualTo(course.getId());
    }

    @Test
    void shouldFindEnrollmentsByUserId() {
        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .roleType(RoleType.USER)
                .build();

        User savedUser = entityManager.persist(user);

        Course course1 = Course.builder()
                .creator(savedUser)
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .averageRating(0.0)
                .build();

        Course course2 = Course.builder()
                .creator(savedUser)
                .title("Spring Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .averageRating(0.0)
                .build();

        Course savedCourse1 = entityManager.persist(course1);
        Course savedCourse2 = entityManager.persist(course2);

        entityManager.persist(Enrollment.builder().user(savedUser).course(savedCourse1).build());
        entityManager.persist(Enrollment.builder().user(savedUser).course(savedCourse2).build());

        entityManager.flush();
        entityManager.clear();

        Page<Enrollment> result = repository.findByUserId(savedUser.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(enrollment -> enrollment.getCourse().getTitle())
                .containsExactlyInAnyOrder("Java Course", "Spring Course");
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
                .creator(savedUser)
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .averageRating(0.0)
                .build();

        Course savedCourse = entityManager.persist(course);

        Enrollment enrollment = Enrollment.builder()
                .user(savedUser)
                .course(savedCourse)
                .build();

        Enrollment savedEnrollment = entityManager.persist(enrollment);

        entityManager.flush();
        entityManager.clear();

        Optional<Enrollment> result = repository.findByIdAndUserId(savedEnrollment.getId(), savedUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedEnrollment.getId());
    }

    @Test
    void shouldFindEnrollmentByCourseIdAndUserId() {
        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .roleType(RoleType.USER)
                .build();

        User savedUser = entityManager.persist(user);

        Course course = Course.builder()
                .creator(savedUser)
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .averageRating(0.0)
                .build();

        Course savedCourse = entityManager.persist(course);

        Enrollment enrollment = Enrollment.builder().user(savedUser).course(savedCourse).build();

        Enrollment savedEnrollment = entityManager.persist(enrollment);

        entityManager.flush();
        entityManager.clear();

        Optional<Enrollment> result = repository.findByCourseIdAndUserId(savedCourse.getId(), savedUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedEnrollment.getId());
        assertThat(result.get().getCourse().getId()).isEqualTo(savedCourse.getId());
        assertThat(result.get().getUser().getId()).isEqualTo(savedUser.getId());
    }
}