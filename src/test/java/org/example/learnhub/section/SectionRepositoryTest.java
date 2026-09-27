package org.example.learnhub.section;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SectionRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private LessonRepository lessonRepository;

    private Course course;

    @BeforeEach
    void setUp() {
        User creator = entityManager.persist(User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
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
    }

    @Test
    void shouldPersistAndReadSectionWithLessonsSuccessfully() {
        Section section = persistSection("Introduction", 1);
        Lesson firstLesson = entityManager.persist(newLesson(section, "https://example.com/one", 120, 1));
        Lesson secondLesson = entityManager.persist(newLesson(section, "https://example.com/two", 180, 2));

        entityManager.flush();
        entityManager.clear();

        Section result = sectionRepository.findById(section.getId()).orElseThrow();
        List<Lesson> lessons = lessonRepository.findAllBySectionId(section.getId());

        assertThat(result.getTitle()).isEqualTo("Introduction");
        assertThat(result.getPosition()).isEqualTo(1);
        assertThat(result.getCourseId()).isEqualTo(course.getId());
        assertThat(lessons)
                .extracting(Lesson::getId)
                .containsExactlyInAnyOrder(firstLesson.getId(), secondLesson.getId());
        assertThat(lessons)
                .extracting(Lesson::getDuration)
                .containsExactlyInAnyOrder(120, 180);
    }

    @Test
    void shouldCalculateCourseLessonDurationSuccessfully() {
        Section section = persistSection("Introduction", 1);
        entityManager.persist(newLesson(section, "https://example.com/one", 120, 1));
        entityManager.persist(newLesson(section, "https://example.com/two", 180, 2));
        entityManager.flush();
        entityManager.clear();

        assertThat(sectionRepository.calculateDurationByCourseId(course.getId())).isEqualTo(300);
    }

    @Test
    void shouldReturnZeroDurationWhenCourseHasNoLessons() {
        assertThat(sectionRepository.calculateDurationByCourseId(course.getId())).isZero();
    }

    @Test
    void shouldCountSectionsByCourseId() {
        persistSection("First", 1);
        persistSection("Second", 2);
        entityManager.flush();
        entityManager.clear();

        assertThat(sectionRepository.countByCourseId(course.getId())).isEqualTo(2);
    }

    @Test
    void shouldReturnSectionsOrderedByPosition() {
        Section second = persistSection("Second", 2);
        Section first = persistSection("First", 1);
        entityManager.flush();
        entityManager.clear();

        List<Section> result = sectionRepository.findAllByCourseIdOrderByPositionAsc(course.getId());

        assertThat(result)
                .extracting(Section::getId)
                .containsExactly(first.getId(), second.getId());
    }

    private Section persistSection(String title, int position) {
        return entityManager.persist(Section.builder()
                .title(title)
                .position(position)
                .courseId(course.getId())
                .build());
    }

    private Lesson newLesson(Section section, String url, int duration, int position) {
        return Lesson.builder()
                .section(section)
                .contentUrl(url)
                .duration(duration)
                .position(position)
                .build();
    }
}
