package org.example.learnhub.section;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.section.entity.Lesson;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.repository.LessonRepository;
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
import java.util.List;

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
    private SectionRepository sectionRepository;

    @Autowired
    private LessonRepository lessonRepository;

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
                .creatorId(creator.getId())
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .build();

        entityManager.persist(course);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldPersistAndReadSectionWithLessonsSuccessfully() {
        Section section = Section.builder()
                .title("Introduction")
                .position(1)
                .courseId(course.getId())
                .build();

        entityManager.persist(section);
        entityManager.flush();

        Lesson firstLesson = Lesson.builder()
                .section(section)
                .contentUrl("https://example.com/one")
                .duration(120)
                .position(1)
                .build();

        Lesson secondLesson = Lesson.builder()
                .section(section)
                .contentUrl("https://example.com/two")
                .duration(180)
                .position(2)
                .build();

        entityManager.persist(firstLesson);
        entityManager.persist(secondLesson);

        entityManager.flush();
        entityManager.clear();

        Section result = sectionRepository.findById(section.getId()).orElseThrow();
        List<Lesson> lessons = lessonRepository.findAllBySectionId(section.getId());

        assertThat(result.getTitle()).isEqualTo("Introduction");
        assertThat(result.getPosition()).isEqualTo(1);
        assertThat(result.getCourseId()).isEqualTo(course.getId());

        assertThat(lessons.get(0).getContentUrl()).isEqualTo("https://example.com/one");
        assertThat(lessons.get(0).getDuration()).isEqualTo(120);
        assertThat(lessons.get(0).getPosition()).isEqualTo(1);
        assertThat(lessons.get(1).getContentUrl()).isEqualTo("https://example.com/two");
        assertThat(lessons.get(1).getDuration()).isEqualTo(180);
        assertThat(lessons.get(1).getPosition()).isEqualTo(2);
    }

    @Test
    void shouldCalculateCourseLessonDurationSuccessfully() {
        Section section = Section.builder()
                .title("Introduction")
                .position(1)
                .courseId(course.getId())
                .build();

        entityManager.persist(section);
        entityManager.flush();

        Lesson firstLesson = Lesson.builder()
                .section(section)
                .contentUrl("https://example.com/one")
                .duration(120)
                .position(1)
                .build();

        Lesson secondLesson = Lesson.builder()
                .section(section)
                .contentUrl("https://example.com/two")
                .duration(180)
                .position(2)
                .build();

        entityManager.persist(firstLesson);
        entityManager.persist(secondLesson);

        entityManager.flush();
        entityManager.clear();

        Integer result = sectionRepository.calculateDurationByCourseId(course.getId());

        assertThat(result).isEqualTo(300);
    }
}