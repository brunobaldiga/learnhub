package org.example.learnhub.course;

import org.example.learnhub.course.dto.*;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.repository.CourseRepository;
import org.example.learnhub.course.service.CourseMapper;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.exception.CourseAccessDenied;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.exception.MaxSectionsReached;
import org.example.learnhub.gateway.PaymentGateway;
import org.example.learnhub.gateway.SectionGateway;
import org.example.learnhub.gateway.UserGateway;
import org.example.learnhub.gateway.dto.SectionInfo;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.user.dto.RoleType;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {
    @Mock
    private CourseRepository repository;

    @Mock
    private SectionGateway sectionGateway;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private CourseMapper mapper;

    @Mock
    private UserGateway userGateway;

    @InjectMocks
    private CourseService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).username("John").roleType(RoleType.CREATOR).build();
    }

    @Test
    void shouldCreateCourseSuccessfully() {
        CourseRequest request = new CourseRequest("Java Course");

        Course course = Course.builder()
                .id(1)
                .creatorId(user.getId())
                .title("Java Course")
                .status(CourseStatus.PRIVATE)
                .price(BigDecimal.ZERO)
                .build();

        CourseResponse response = new CourseResponse(
                1,
                user.getId(),
                user.getUsername(),
                course.getTitle(),
                course.getStatus(),
                course.getPrice(),
                0,
                LocalDateTime.now()
        );

        when(mapper.toCourse(request)).thenReturn(course);
        when(mapper.toDto(course, user.getUsername())).thenReturn(response);

        CourseResponse result = service.create(user, request);

        assertThat(result).isEqualTo(response);
    }


    @Test
    void shouldReturnCourseWhenCourseExists() {
        Course course = Course.builder()
                .id(1)
                .creatorId(user.getId())
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.ZERO)
                .build();

        CourseResponse response = new CourseResponse(
                1,
                1,
                "John",
                "Java Course",
                CourseStatus.PUBLIC,
                BigDecimal.ZERO,
                0,
                LocalDateTime.now()
        );

        when(mapper.toDto(course, user.getUsername())).thenReturn(response);
        when(repository.findByIdAndCreatorId(1, user.getId())).thenReturn(Optional.of(course));
        when(userGateway.findUsernameById(user.getId())).thenReturn(user.getUsername());

        CourseResponse result = service.findCourseById(user, course.getId());

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldReturn404WhenCourseNotFound() {
        when(repository.findByIdAndStatus(any(), any())).thenReturn(Optional.empty());
        when(repository.findByIdAndCreatorId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findCourseById(user, 1))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Course not found.");
    }

    @Test
    void shouldUpdateCourseSuccessfully() {
        Course course = Course.builder()
                .id(1)
                .creatorId(user.getId())
                .title("Java Course")
                .status(CourseStatus.PRIVATE)
                .price(BigDecimal.ZERO)
                .build();

        UpdateCourseRequest request = new UpdateCourseRequest("Updated Java Course", CourseStatus.PUBLIC, BigDecimal.valueOf(20));

        CourseResponse response = new CourseResponse(
                1,
                user.getId(),
                user.getUsername(),
                "Updated Java Course",
                CourseStatus.PUBLIC,
                BigDecimal.valueOf(20),
                0,
                LocalDateTime.now()
        );

        when(repository.findByIdAndCreatorId(any(), any())).thenReturn(Optional.of(course));
        when(userGateway.findUsernameById(user.getId())).thenReturn(user.getUsername());
        when(mapper.toDto(course, user.getUsername())).thenReturn(response);

        CourseResponse result = service.updateCourseById(user, course.getId(), request);

        verify(repository).save(course);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldReturn404WhenCourseNotFoundOnUpdate() {
        UpdateCourseRequest request = new UpdateCourseRequest("Updated Java Course", CourseStatus.PUBLIC, BigDecimal.valueOf(20));

        when(repository.findByIdAndCreatorId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCourseById(user, 1, request))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Course not found.");

    }

    @Test
    void shouldCreateSectionSuccessfully() {
        Course course = Course.builder().id(1).creatorId(user.getId()).build();

        SectionInfo sectionInfo = new SectionInfo(1, "Section 1", 0, course.getId());
        SectionRequest request = new SectionRequest("Section 1", 1);
        SectionResponse response = new SectionResponse(1, "Section 1", 0, List.of());

        when(repository.findByIdAndCreatorId(any(), any())).thenReturn(Optional.of(course));
        when(sectionGateway.countSectionsByCourseId(course.getId())).thenReturn(1);
        when(sectionGateway.create(any(), any())).thenReturn(sectionInfo);

        SectionResponse result = service.createCourseSection(user, course.getId(), request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldReturn409WhenSectionLimitReached() {
        Course course = Course.builder().id(1).creatorId(user.getId()).build();

        SectionRequest request = new SectionRequest("Section 20", 20);

        when(repository.findByIdAndCreatorId(any(), any())).thenReturn(Optional.of(course));
        when(sectionGateway.countSectionsByCourseId(course.getId())).thenReturn(20);

        assertThatThrownBy(() -> service.createCourseSection(user, course.getId(), request))
                .isInstanceOf(MaxSectionsReached.class)
                .hasMessage("Course cannot have more than 20 sections.");
    }

    @Test
    void shouldReturn404WhenCourseNotFoundOnSectionCreate() {
        SectionRequest request = new SectionRequest("Section 20", 20);

        when(repository.findByIdAndCreatorId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createCourseSection(user, 1, request))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Course not found.");
    }

    @Test
    void shouldReturnSectionWhenUserIsOwner() {
        SectionResponse sectionResponse = new SectionResponse(1, "Section 1", 0, List.of());

        Course course = Course.builder().id(1).creatorId(user.getId()).build();

        when(repository.findByIdAndStatus(any(), any())).thenReturn(Optional.of(course));
        when(sectionGateway.findAllByCourseId(course.getId())).thenReturn(List.of(sectionResponse));

        List<SectionResponse> result = service.findCourseSection(user, course.getId());

        assertThat(result).isEqualTo(List.of(sectionResponse));
    }

    @Test
    void shouldReturnSectionWhenUserHasPaid() {
        User creator = User.builder().id(2).build();

        SectionResponse sectionResponse = new SectionResponse(1, "Section 1", 0, List.of());

        Course course = Course.builder().id(1).creatorId(creator.getId()).build();

        when(repository.findByIdAndStatus(any(), any())).thenReturn(Optional.of(course));
        when(paymentGateway.existsByUserIdAndCourseId(any(), any())).thenReturn(true);
        when(sectionGateway.findAllByCourseId(course.getId())).thenReturn(List.of(sectionResponse));

        List<SectionResponse> result = service.findCourseSection(user, course.getId());

        assertThat(result).isEqualTo(List.of(sectionResponse));
    }

    @Test
    void shouldReturn403WhenUserHasNotPaid() {
        User creator = User.builder().id(2).build();

        Course course = Course.builder().id(1).creatorId(creator.getId()).build();

        when(repository.findByIdAndStatus(any(), any())).thenReturn(Optional.of(course));
        when(paymentGateway.existsByUserIdAndCourseId(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.findCourseSection(user, course.getId()))
                .isInstanceOf(CourseAccessDenied.class)
                .hasMessage("User haven't paid for the course.");
    }

    @Test
    void shouldSearchCoursesSuccessfully() {
        Course course = Course.builder().id(1).title("Java").creatorId(user.getId()).build();

        CourseResponse response = new CourseResponse(
                1,
                1,
                "John",
                "Java",
                CourseStatus.PUBLIC,
                BigDecimal.ZERO,
                0,
                LocalDateTime.now()
        );

        Page<Course> page = new PageImpl<>(List.of(course));
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(userGateway.findUsernamesByIds(Set.of(user.getId()))).thenReturn(Map.of(user.getId(), "John"));
        when(mapper.toDto(course, user.getUsername())).thenReturn(response);

        Page<CourseResponse> result = service.search(new CourseFilter(null, "John"), pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void shouldFindOwnedCoursesSuccessfully() {
        Course course = Course.builder().id(1).creatorId(user.getId()).build();

        CourseResponse response = new CourseResponse(
                1,
                1,
                "John",
                "Java",
                CourseStatus.PUBLIC,
                BigDecimal.ZERO,
                0,
                LocalDateTime.now()
        );

        Page<Course> page = new PageImpl<>(List.of(course));
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(userGateway.findUsernamesByIds(Set.of(user.getId()))).thenReturn(Map.of(user.getId(), "John"));
        when(mapper.toDto(course, user.getUsername())).thenReturn(response);

        Page<CourseResponse> result = service.findUserCourses(user, new CourseFilter(null, "John"), pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void shouldCountLessonsByCourseId() {
        when(repository.countLessonsByCourseId(1)).thenReturn(10);

        Integer result = service.countLessonsByCourseId(1);

        assertThat(result).isEqualTo(10);
    }

    @Test
    void shouldUpdateSectionSuccessfully() {
        SectionRequest request = new SectionRequest("New Title", 1);
        SectionResponse response = new SectionResponse(1, "New Title", 1, List.of());

        when(sectionGateway.update(1, user.getId(), request)).thenReturn(response);

        SectionResponse result = service.updateCourseSection(user, 1, request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldDeleteSectionSuccessfully() {
        service.deleteCourseSection(user, 1);

        verify(sectionGateway).delete(1, user.getId());
    }
}
