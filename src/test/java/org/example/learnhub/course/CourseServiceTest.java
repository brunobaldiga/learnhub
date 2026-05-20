package org.example.learnhub.course;

import org.example.learnhub.course.dto.CourseRequest;
import org.example.learnhub.course.dto.CourseResponse;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.gateway.SectionGateway;
import org.example.learnhub.course.repository.CourseRepository;
import org.example.learnhub.course.service.CourseMapper;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {
    @Mock
    private CourseRepository repository;

    @Mock
    private SectionGateway sectionGateway;

    @Mock
    private CourseMapper mapper;

    @InjectMocks
    private CourseService service;

    @Test
    void shouldCreateCourseSuccessfully() {
        User user = User.builder()
                .id(1)
                .username("John")
                .roleType(RoleType.CREATOR)
                .build();

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
        User user = User.builder()
                .id(1)
                .username("John")
                .roleType(RoleType.CREATOR)
                .build();

        Course course = Course.builder()
                .id(1)
                .creator(user)
                .title("Java Course")
                .status(CourseStatus.PRIVATE)
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

    }

    @Test
    void shouldUpdateCourseSuccessfully() {

    }

    @Test
    void shouldThrowWhenCourseNotFoundOnUpdate() {

    }

    @Test
    void shouldCreateSectionSuccessfully() {

    }

    @Test
    void shouldThrowWhenSectionLimitReached() {

    }

    @Test
    void shouldThrowWhenCourseNotFoundOnSectionCreate() {

    }

    @Test
    void shouldReturnSectionWhenUserIsOwner() {

    }

    @Test
    void shouldReturnSectionWhenUserHasPaid() {

    }

    @Test
    void shouldThrowWhenUserHasNotPaid() {

    }
}
