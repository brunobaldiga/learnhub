package org.example.learnhub.course;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.repository.CourseRepository;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@DataJpaTest
public class CourseRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseRepository repository;

    @Test
    void shouldReturnCourseByIdAndStatus() {
        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .roleType(RoleType.CREATOR)
                .build();

        entityManager.persist(user);

        Course course = Course.builder()
                .creatorId(user.getId())
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .build();

        entityManager.persist(course);

        entityManager.flush();
        entityManager.clear();

        Optional<Course> result = repository.findByIdAndStatus(course.getId(), CourseStatus.PUBLIC);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(course.getId());
        assertThat(result.get().getStatus()).isEqualTo(CourseStatus.PUBLIC);
    }

    @Test
    void shouldReturnEmptyWhenCourseStatusDoesNotMatch() {
        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .roleType(RoleType.CREATOR)
                .build();

        entityManager.persist(user);

        Course course = Course.builder()
                .creatorId(user.getId())
                .title("Java Course")
                .status(CourseStatus.PRIVATE)
                .price(BigDecimal.TEN)
                .build();

        entityManager.persist(course);

        entityManager.flush();
        entityManager.clear();

        Optional<Course> result = repository.findByIdAndStatus(course.getId(), CourseStatus.PUBLIC);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnCourseByIdAndCreatorId() {
        User creator = User.builder()
                .username("course_creator")
                .email("course_creator@example.com")
                .fullName("Jane Doe")
                .password("password")
                .roleType(RoleType.CREATOR)
                .build();

        entityManager.persist(creator);

        Course course = Course.builder()
                .creatorId(creator.getId())
                .title("Java Course")
                .status(CourseStatus.PRIVATE)
                .price(BigDecimal.TEN)
                .build();

        entityManager.persist(course);

        entityManager.flush();
        entityManager.clear();

        Optional<Course> result = repository.findByIdAndCreatorId(course.getId(), creator.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(course.getId());
        assertThat(result.get().getCreatorId()).isEqualTo(creator.getId());
    }

    @Test
    void shouldReturnEmptyWhenCourseDoesNotBelongToCreator() {
        User creator = User.builder()
                .username("course_creator")
                .email("course_creator@example.com")
                .fullName("Jane Doe")
                .password("password")
                .roleType(RoleType.CREATOR)
                .build();

        User otherUser = User.builder()
                .username("other")
                .email("other@example.com")
                .fullName("John Doe")
                .password("password")
                .roleType(RoleType.CREATOR)
                .build();

        entityManager.persist(creator);
        entityManager.persist(otherUser);

        Course course = Course.builder()
                .creatorId(otherUser.getId())
                .title("Java Course")
                .status(CourseStatus.PRIVATE)
                .price(BigDecimal.TEN)
                .build();

        entityManager.persist(course);

        entityManager.flush();
        entityManager.clear();

        Optional<Course> result = repository.findByIdAndCreatorId(course.getId(), otherUser.getId());

        assertThat(result).isEmpty();
    }
}
