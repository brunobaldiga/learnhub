package org.example.learnhub.course;

import org.example.learnhub.course.dto.*;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.repository.CourseRepository;
import org.example.learnhub.course.service.CourseMapper;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.exception.CourseAccessDeniedException;
import org.example.learnhub.exception.EntityNotFoundException;
import org.example.learnhub.exception.MaxSectionsReachedException;
import org.example.learnhub.gateway.PaymentGateway;
import org.example.learnhub.gateway.SectionGateway;
import org.example.learnhub.gateway.UserGateway;
import org.example.learnhub.gateway.dto.CourseSummary;
import org.example.learnhub.gateway.dto.SectionInfo;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    @Mock
    CourseRepository repository;
    @Mock
    CourseMapper mapper;
    @Mock
    SectionGateway sectionGateway;
    @Mock
    PaymentGateway paymentGateway;
    @Mock
    UserGateway userGateway;

    @InjectMocks
    CourseService service;

    private User creator;
    private Course course;

    @BeforeEach
    void setUp() {
        creator = User.builder().id(1).username("creator").build();
        course = Course.builder()
                .id(10)
                .creatorId(creator.getId())
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.valueOf(99.99))
                .currency(CurrencyCode.USD)
                .salesAmount(3)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private CourseResponse responseFor(Course value, String username) {
        return new CourseResponse(
                value.getId(), value.getCreatorId(), username, value.getTitle(), value.getStatus(),
                value.getPrice(), value.getCurrency(), value.getSalesAmount(), value.getCreatedAt()
        );
    }

    @Test
    void shouldCreateCourseSuccessfully() {
        CourseRequest request = new CourseRequest("Java Course");
        Course mapped = Course.builder().title("Java Course").build();
        CourseResponse response = responseFor(course, creator.getUsername());

        when(mapper.toCourse(request)).thenReturn(mapped);
        when(mapper.toDto(mapped, creator.getUsername())).thenReturn(response);

        assertThat(service.create(creator, request)).isEqualTo(response);
        assertThat(mapped.getCreatorId()).isEqualTo(creator.getId());
        verify(repository).save(mapped);
    }

    @Test
    void shouldSearchPublicCoursesAndResolveCreatorNames() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(List.of(course), pageable, 1);
        CourseResponse response = responseFor(course, "creator");

        when(userGateway.findIdsByUsernameContaining("creat")).thenReturn(List.of(creator.getId()));
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(userGateway.findUsernamesByIds(Set.of(creator.getId())))
                .thenReturn(Map.of(creator.getId(), "creator"));
        when(mapper.toDto(course, "creator")).thenReturn(response);

        Page<CourseResponse> result = service.search(new CourseFilter("Java", "creat"), pageable);

        assertThat(result.getContent()).containsExactly(response);
        verify(userGateway).findIdsByUsernameContaining("creat");
    }

    @Test
    void shouldNotSearchCreatorIdsWhenCreatorFilterIsBlank() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(Page.empty(pageable));
        when(userGateway.findUsernamesByIds(Set.of())).thenReturn(Map.of());

        service.search(new CourseFilter(null, "   "), pageable);

        verify(userGateway, never()).findIdsByUsernameContaining(any());
    }

    @Test
    void shouldFindCreatorCourses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(List.of(course), pageable, 1);
        CourseResponse response = responseFor(course, "creator");

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(userGateway.findUsernamesByIds(Set.of(creator.getId())))
                .thenReturn(Map.of(creator.getId(), "creator"));
        when(mapper.toDto(course, "creator")).thenReturn(response);

        assertThat(service.findUserCourses(creator, new CourseFilter(null, null), pageable).getContent())
                .containsExactly(response);
    }

    @Test
    void shouldFindCourseById() {
        CourseResponse response = responseFor(course, "creator");
        when(repository.findById(course.getId())).thenReturn(Optional.of(course));
        when(userGateway.findUsernameById(creator.getId())).thenReturn("creator");
        when(mapper.toDto(course, "creator")).thenReturn(response);

        assertThat(service.findById(creator, course.getId())).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenCourseDoesNotExist() {
        when(repository.findById(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(creator, 10))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Course not found.");
    }

    @Test
    void shouldFindOwnedCourse() {
        when(repository.findByIdAndCreatorId(10, 1)).thenReturn(Optional.of(course));
        assertThat(service.findOwnedCourseByIdAndCreatorId(10, 1)).isSameAs(course);
    }

    @Test
    void shouldFindPublicCourse() {
        when(repository.findByIdAndStatus(10, CourseStatus.PUBLIC)).thenReturn(Optional.of(course));
        assertThat(service.findPublicCourseById(10)).isSameAs(course);
    }

    @Test
    void shouldUpdateOwnedCourse() {
        UpdateCourseRequest request = new UpdateCourseRequest(
                "Updated", CourseStatus.PRIVATE, BigDecimal.TEN, CurrencyCode.BRL
        );
        CourseResponse response = responseFor(course, creator.getUsername());

        when(repository.findByIdAndCreatorId(10, 1)).thenReturn(Optional.of(course));
        when(mapper.toDto(course, creator.getUsername())).thenReturn(response);

        assertThat(service.updateById(creator, 10, request)).isEqualTo(response);
        verify(mapper).updateCourse(course, request);
    }

    @Test
    void shouldThrowWhenUpdatingCourseNotOwnedByCreator() {
        UpdateCourseRequest request = new UpdateCourseRequest("Updated", null, null, null);
        when(repository.findByIdAndCreatorId(10, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateById(creator, 10, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Course not found.");
    }

    @Test
    void shouldCreateSection() {
        SectionRequest request = new SectionRequest("Introduction", 1);
        SectionInfo info = new SectionInfo(20, "Introduction", 1, 10);

        when(repository.findByIdAndCreatorId(10, 1)).thenReturn(Optional.of(course));
        when(sectionGateway.countSectionsByCourseId(10)).thenReturn(5L);
        when(sectionGateway.create(request, 10)).thenReturn(info);

        assertThat(service.createCourseSection(creator, 10, request))
                .isEqualTo(new SectionResponse(20, "Introduction", 1, List.of()));
    }

    @Test
    void shouldRejectTwentyFirstSection() {
        SectionRequest request = new SectionRequest("Introduction", 20);
        when(repository.findByIdAndCreatorId(10, 1)).thenReturn(Optional.of(course));
        when(sectionGateway.countSectionsByCourseId(10)).thenReturn(20L);

        assertThatThrownBy(() -> service.createCourseSection(creator, 10, request))
                .isInstanceOf(MaxSectionsReachedException.class)
                .hasMessage("Course cannot have more than 20 sections.");
        verify(sectionGateway, never()).create(any(), any());
    }

    @Test
    void shouldListSectionsForCourseOwnerWithoutPayment() {
        SectionResponse section = new SectionResponse(20, "Introduction", 1, List.of());
        when(repository.findById(10)).thenReturn(Optional.of(course));
        when(paymentGateway.existsByUserIdAndCourseId(1, 10)).thenReturn(false);
        when(sectionGateway.findAllByCourseId(10)).thenReturn(List.of(section));

        assertThat(service.findCourseSection(creator, 10)).containsExactly(section);
    }

    @Test
    void shouldListSectionsForPayingStudent() {
        User student = User.builder().id(2).build();
        SectionResponse section = new SectionResponse(20, "Introduction", 1, List.of());
        when(repository.findById(10)).thenReturn(Optional.of(course));
        when(paymentGateway.existsByUserIdAndCourseId(2, 10)).thenReturn(true);
        when(sectionGateway.findAllByCourseId(10)).thenReturn(List.of(section));

        assertThat(service.findCourseSection(student, 10)).containsExactly(section);
    }

    @Test
    void shouldRejectSectionsForUnpaidNonOwner() {
        User student = User.builder().id(2).build();
        when(repository.findById(10)).thenReturn(Optional.of(course));
        when(paymentGateway.existsByUserIdAndCourseId(2, 10)).thenReturn(false);

        assertThatThrownBy(() -> service.findCourseSection(student, 10))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessage("User haven't paid for the course.");
    }

    @Test
    void shouldUpdateSectionWhenUserOwnsCourse() {
        SectionRequest request = new SectionRequest("Updated", 2);
        SectionInfo info = new SectionInfo(20, "Introduction", 1, 10);
        SectionResponse response = new SectionResponse(20, "Updated", 2, List.of());

        when(sectionGateway.findById(20)).thenReturn(info);
        when(repository.existsByIdAndCreatorId(10, 1)).thenReturn(true);
        when(sectionGateway.update(20, request)).thenReturn(response);

        assertThat(service.updateCourseSection(creator, 20, request)).isEqualTo(response);
    }

    @Test
    void shouldRejectSectionUpdateByNonOwner() {
        SectionRequest request = new SectionRequest("Updated", 2);
        when(sectionGateway.findById(20)).thenReturn(new SectionInfo(20, "Intro", 1, 10));
        when(repository.existsByIdAndCreatorId(10, 1)).thenReturn(false);

        assertThatThrownBy(() -> service.updateCourseSection(creator, 20, request))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessage("You do not own this course.");
        verify(sectionGateway, never()).update(any(), any());
    }

    @Test
    void shouldDeleteSectionWhenUserOwnsCourse() {
        when(sectionGateway.findById(20)).thenReturn(new SectionInfo(20, "Intro", 1, 10));
        when(repository.existsByIdAndCreatorId(10, 1)).thenReturn(true);

        service.deleteCourseSection(creator, 20);

        verify(sectionGateway).delete(20);
    }

    @Test
    void shouldIncrementSalesAmount() {
        when(repository.findById(10)).thenReturn(Optional.of(course));

        service.incrementSalesAmount(10);

        assertThat(course.getSalesAmount()).isEqualTo(4);
        verify(repository).save(course);
    }

    @Test
    void shouldRecordReview() {
        when(repository.findById(10)).thenReturn(Optional.of(course));

        service.recordReview(10, 5);

        assertThat(course.getTotalReviews()).isEqualTo(1);
        assertThat(course.getAverageRating()).isEqualTo(5.0);
        verify(repository).save(course);
    }

    @Test
    void shouldCountLessons() {
        when(repository.countLessonsByCourseId(10)).thenReturn(7);
        assertThat(service.countLessonsByCourseId(10)).isEqualTo(7);
    }

    @Test
    void shouldReturnCourseSummary() {
        when(repository.findById(10)).thenReturn(Optional.of(course));
        when(userGateway.findUsernameById(1)).thenReturn("creator");

        assertThat(service.findCourseSummaryById(10))
                .isEqualTo(new CourseSummary(10, "Java Course", "creator"));
    }

    @Test
    void shouldReturnCourseSummariesInBulk() {
        Course second = Course.builder().id(11).creatorId(2).title("Spring").build();
        when(repository.findAllById(List.of(10, 11))).thenReturn(List.of(course, second));
        when(userGateway.findUsernamesByIds(Set.of(1, 2)))
                .thenReturn(Map.of(1, "creator", 2, "secondCreator"));

        Map<Integer, CourseSummary> result = service.findCourseSummariesById(List.of(10, 11));

        assertThat(result).containsEntry(10, new CourseSummary(10, "Java Course", "creator"));
        assertThat(result).containsEntry(11, new CourseSummary(11, "Spring", "secondCreator"));
    }
}
