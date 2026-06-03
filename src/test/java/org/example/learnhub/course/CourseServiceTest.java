package org.example.learnhub.course;

import org.example.learnhub.course.dto.CourseRequest;
import org.example.learnhub.course.dto.CourseResponse;
import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.dto.UpdateCourseRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.gateway.PaymentGateway;
import org.example.learnhub.gateway.SectionGateway;
import org.example.learnhub.course.repository.CourseRepository;
import org.example.learnhub.course.service.CourseMapper;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private CourseService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).username("John").roleType(RoleType.CREATOR).build();
    }

    @Test
    void shouldCreateCourseSuccessfully() {
        CourseRequest request = new CourseRequest(
            "Java Course"
        );

        Course course = Course.builder()
                .id(1)
                .creator(user)
                .title("Java Course")
                .status(CourseStatus.PRIVATE)
                .price(BigDecimal.ZERO)
                .build();

        CourseResponse response = new CourseResponse(
                1, user.getId(), user.getUsername(), course.getTitle(), course.getStatus(), course.getPrice(), 0, LocalDateTime.now()
        );

        when(mapper.toCourse(request)).thenReturn(course);
        when(mapper.toDto(course)).thenReturn(response);

        CourseResponse result = service.create(user, request);

        assertThat(result).isEqualTo(response);
    }


    @Test
    void shouldReturnCourseWhenCourseExists() {
        Course course = Course.builder()
                .id(1)
                .creator(user)
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.ZERO)
                .build();

        CourseResponse response = new CourseResponse(
                1, 1, "John", "Java Course", CourseStatus.PRIVATE, BigDecimal.ZERO, 0, LocalDateTime.now()
        );

        when(mapper.toDto(course)).thenReturn(response);
        when(repository.findByIdAndCreatorId(1, user.getId())).thenReturn(Optional.of(course));

        CourseResponse result = service.findCourseById(user, course.getId());

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenCourseNotFound() {
        when(repository.findByIdAndStatus(any(), any())).thenReturn(Optional.empty());
        when(repository.findByIdAndCreatorId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findCourseById(user, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Course not found.");
    }

    @Test
    void shouldUpdateCourseSuccessfully() {
        Course course = Course.builder()
                .id(1)
                .creator(user)
                .title("Java Course")
                .status(CourseStatus.PRIVATE)
                .price(BigDecimal.ZERO)
            .build();

        UpdateCourseRequest request = new UpdateCourseRequest(
                "Updated Java Course", CourseStatus.PUBLIC, BigDecimal.valueOf(20)
        );

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
        when(mapper.toDto(course)).thenReturn(response);

        CourseResponse result = service.updateCourseById(user, course.getId(), request);

        verify(repository).save(course);
        assertThat(result).isEqualTo(response);

    }

    @Test
    void shouldThrowWhenCourseNotFoundOnUpdate() {
        UpdateCourseRequest request = new UpdateCourseRequest(
                "Updated Java Course", CourseStatus.PUBLIC, BigDecimal.valueOf(20)
        );

        when(repository.findByIdAndCreatorId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCourseById(user, 1, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Course not found.");

    }

    @Test
    void shouldCreateSectionSuccessfully() {
        Course course = Course.builder()
                .id(1)
                .creator(user)
                .sections(new ArrayList<>())
            .build();

        Section section = Section.builder()
                .id(1)
                .title("Section 1")
                .index(0)
                .course(course)
                .build();

        SectionRequest request = new SectionRequest(
                "Section 1",
                0
        );

        SectionResponse response = new SectionResponse(
                1, "Section 1", 0, List.of()
        );

        when(repository.findByIdAndCreatorId(any(), any())).thenReturn(Optional.of(course));
        when(sectionGateway.saveSection(any(), any())).thenReturn(section);
        when(sectionGateway.toDto(any())).thenReturn(response);

        SectionResponse result = service.createCourseSection(user, course.getId(), request);

        verify(repository).save(course);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenSectionLimitReached() {
        List<Section> sections = new ArrayList<>(Collections.nCopies(20, new Section()));

        Course course = Course.builder()
                .id(1)
                .creator(user)
                .sections(sections)
        .build();

        SectionRequest request = new SectionRequest(
                "Section 21",
                21
        );

        when(repository.findByIdAndCreatorId(any(), any())).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.createCourseSection(user, course.getId(), request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Course cannot have more than 20 sections.");
    }

    @Test
    void shouldThrowWhenCourseNotFoundOnSectionCreate() {
        SectionRequest request = new SectionRequest(
                "Section 21",
                21
        );

        when(repository.findByIdAndCreatorId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createCourseSection(user, 1, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Course not found.");
    }

    @Test
    void shouldReturnSectionWhenUserIsOwner() {
        Section section = Section.builder().id(1).title("Section 1").index(0).build();
        List<Section> sections = new ArrayList<>(List.of(section));
        SectionResponse sectionResponse = new SectionResponse(1, "Section 1", 0, List.of());

        Course course = Course.builder()
                .id(1)
                .creator(user)
                .sections(sections)
                .build();


        when(repository.findByIdAndStatus(any(), any())).thenReturn(Optional.of(course));
        when(paymentGateway.existsByUserIdAndCourseId(any(), any())).thenReturn(false);
        when(sectionGateway.toDto(section)).thenReturn(sectionResponse);

        List<SectionResponse> result = service.findCourseSection(user, course.getId());

        assertThat(result).isEqualTo(List.of(sectionResponse));
    }

    @Test
    void shouldReturnSectionWhenUserHasPaid() {
        User creator = User.builder().id(2).build();

        Section section = Section.builder().id(1).title("Section 1").index(0).build();
        List<Section> sections = new ArrayList<>(List.of(section));
        SectionResponse sectionResponse = new SectionResponse(1, "Section 1", 0, List.of());

        Course course = Course.builder()
                .id(1)
                .creator(creator)
                .sections(sections)
                .build();

        when(repository.findByIdAndStatus(any(), any())).thenReturn(Optional.of(course));
        when(paymentGateway.existsByUserIdAndCourseId(any(), any())).thenReturn(true);
        when(sectionGateway.toDto(section)).thenReturn(sectionResponse);

        List<SectionResponse> result = service.findCourseSection(user, course.getId());

        assertThat(result).isEqualTo(List.of(sectionResponse));
    }

    @Test
    void shouldThrowWhenUserHasNotPaid() {
        User creator = User.builder().id(2).build();

        Section section = Section.builder().id(1).title("Section 1").index(0).build();
        List<Section> sections = new ArrayList<>(List.of(section));
        SectionResponse sectionResponse = new SectionResponse(1, "Section 1", 0, List.of());

        Course course = Course.builder()
                .id(1)
                .creator(creator)
                .sections(sections)
                .build();

        when(repository.findByIdAndStatus(any(), any())).thenReturn(Optional.of(course));
        when(paymentGateway.existsByUserIdAndCourseId(any(), any())).thenReturn(false);


        assertThatThrownBy(() -> service.findCourseSection(user, course.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User haven't paid for the course");
    }
}
