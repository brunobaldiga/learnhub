package org.example.learnhub.course;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.repository.CourseRepository;
import org.example.learnhub.section.entity.Lesson;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@DataJpaTest
public class CourseRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseRepository repository;

    @Test
    void shouldCountLessonsByCourseId() {
        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .roleType(RoleType.CREATOR)
                .build();

        entityManager.persist(user);

        Course course = Course.builder()
                .creator(user)
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .averageRating(0.0)
                .build();

        entityManager.persist(course);

        Section section = Section.builder()
                .title("Introduction")
                .position(1)
                .course(course)
                .build();

        entityManager.persist(section);

        Lesson lesson1 = Lesson.builder()
                .section(section)
                .contentUrl("https://youtube.com/video1")
                .duration(100)
                .position(1)
                .build();

        Lesson lesson2 = Lesson.builder()
                .section(section)
                .contentUrl("https://youtube.com/video2")
                .duration(200)
                .position(2)
                .build();

        Lesson lesson3 = Lesson.builder()
                .section(section)
                .contentUrl("https://youtube.com/video3")
                .duration(300)
                .position(3)
                .build();

        entityManager.persist(lesson1);
        entityManager.persist(lesson2);
        entityManager.persist(lesson3);

        entityManager.flush();
        entityManager.clear();

        Integer result = repository.countLessonsByCourseId(course.getId());

        assertThat(result).isEqualTo(3);
    }

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
                .creator(user)
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .averageRating(0.0)
                .build();

        entityManager.persist(course);

        entityManager.flush();
        entityManager.clear();

        Optional<Course> result =
                repository.findByIdAndStatus(
                        course.getId(),
                        CourseStatus.PUBLIC
                );

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
                .creator(user)
                .title("Java Course")
                .status(CourseStatus.PRIVATE)
                .price(BigDecimal.TEN)
                .averageRating(0.0)
                .build();

        entityManager.persist(course);

        entityManager.flush();
        entityManager.clear();

        Optional<Course> result =
                repository.findByIdAndStatus(
                        course.getId(),
                        CourseStatus.PUBLIC
                );

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
                .creator(creator)
                .title("Java Course")
                .status(CourseStatus.PRIVATE)
                .price(BigDecimal.TEN)
                .averageRating(0.0)
                .build();

        entityManager.persist(course);

        entityManager.flush();
        entityManager.clear();

        Optional<Course> result =
                repository.findByIdAndCreatorId(
                        course.getId(),
                        creator.getId()
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(course.getId());
        assertThat(result.get().getCreator().getId()).isEqualTo(creator.getId());
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
                .creator(creator)
                .title("Java Course")
                .status(CourseStatus.PRIVATE)
                .price(BigDecimal.TEN)
                .averageRating(0.0)
                .build();

        entityManager.persist(course);

        entityManager.flush();
        entityManager.clear();

        Optional<Course> result =
                repository.findByIdAndCreatorId(
                        course.getId(),
                        otherUser.getId()
                );

        assertThat(result).isEmpty();
    }
}
