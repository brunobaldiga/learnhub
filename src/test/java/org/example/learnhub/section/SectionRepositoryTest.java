package org.example.learnhub.section;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.repository.SectionRepository;
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
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@DataJpaTest
public class SectionRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SectionRepository repository;

    private User creator;
    private Course course;

    @BeforeEach
    void setUp() {
        creator = User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .roleType(RoleType.CREATOR)
                .build();

        entityManager.persist(creator);

        course = Course.builder()
                .creator(creator)
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
    void shouldReturnSectionByIdAndCourseCreatorId() {
        Section section = Section.builder().title("Introduction").position(1).course(course).build();

        entityManager.persist(section);

        entityManager.flush();
        entityManager.clear();

        Optional<Section> result = repository.findByIdAndCourseCreatorId(
                section.getId(),
                creator.getId()
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(section.getId());
        assertThat(result.get().getCourse().getId()).isEqualTo(course.getId());
    }

    @Test
    void shouldReturnEmptyWhenSectionDoesNotBelongToCreator() {
        User otherCreator = User.builder()
                .username("other")
                .email("other@example.com")
                .fullName("Other Creator")
                .password("password")
                .roleType(RoleType.CREATOR)
                .build();

        entityManager.persist(otherCreator);

        Section section = Section.builder()
                .title("Introduction")
                .position(1)
                .course(course)
                .build();

        entityManager.persist(section);

        entityManager.flush();
        entityManager.clear();

        Optional<Section> result = repository.findByIdAndCourseCreatorId(
                section.getId(),
                otherCreator.getId()
        );

        assertThat(result).isEmpty();
    }
}