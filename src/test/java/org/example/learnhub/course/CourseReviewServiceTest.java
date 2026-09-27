package org.example.learnhub.course;

import org.example.learnhub.course.dto.CourseReviewFilter;
import org.example.learnhub.course.dto.CourseReviewRequest;
import org.example.learnhub.course.dto.CourseReviewResponse;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseReview;
import org.example.learnhub.course.repository.CourseReviewRepository;
import org.example.learnhub.course.service.CourseReviewMapper;
import org.example.learnhub.course.service.CourseReviewService;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.exception.*;
import org.example.learnhub.gateway.EnrollmentGateway;
import org.example.learnhub.gateway.UserGateway;
import org.example.learnhub.gateway.dto.EnrollmentInfo;
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
import org.springframework.data.jpa.domain.Specification;

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
class CourseReviewServiceTest {
    @Mock
    CourseReviewRepository repository;
    @Mock
    CourseService courseService;
    @Mock
    EnrollmentGateway enrollmentGateway;
    @Mock
    CourseReviewMapper mapper;
    @Mock
    UserGateway userGateway;

    @InjectMocks
    CourseReviewService service;

    private User student;
    private Course course;
    private CourseReviewRequest request;

    @BeforeEach
    void setUp() {
        student = User.builder().id(2).username("student").build();
        course = Course.builder().id(10).creatorId(1).totalReviews(1).averageRating(5.0).build();
        request = new CourseReviewRequest(5, "Excellent course");
    }

    @Test
    void shouldCreateReviewForEnrolledStudent() {
        CourseReview review = CourseReview.builder().id(30).course(course).authorId(2).rating(5).comment(request.comment()).build();
        CourseReviewResponse response = new CourseReviewResponse(30, "student", 5, request.comment(), LocalDateTime.now());

        when(courseService.findEntityById(10)).thenReturn(course);
        when(enrollmentGateway.findByUserIdAndCourseId(2, 10))
                .thenReturn(Optional.of(new EnrollmentInfo(5, 2, 10, LocalDateTime.now())));
        when(repository.existsByAuthorIdAndCourseId(2, 10)).thenReturn(false);
        when(mapper.toCourseReview(student, course, request)).thenReturn(review);
        when(mapper.toDto(review, "student")).thenReturn(response);

        assertThat(service.create(student, 10, request)).isEqualTo(response);
        verify(courseService).recordReview(10, 5);
        verify(repository).save(review);
    }

    @Test
    void shouldRejectCreatorReviewingOwnCourse() {
        User creator = User.builder().id(1).build();
        when(courseService.findEntityById(10)).thenReturn(course);
        when(enrollmentGateway.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(creator, 10, request))
                .isInstanceOf(SelfReviewNotAllowedException.class)
                .hasMessage("Course creator cannot review its own course.");
    }

    @Test
    void shouldRejectDuplicateReview() {
        when(courseService.findEntityById(10)).thenReturn(course);
        when(enrollmentGateway.findByUserIdAndCourseId(2, 10))
                .thenReturn(Optional.of(new EnrollmentInfo(5, 2, 10, LocalDateTime.now())));
        when(repository.existsByAuthorIdAndCourseId(2, 10)).thenReturn(true);

        assertThatThrownBy(() -> service.create(student, 10, request))
                .isInstanceOf(DuplicateReviewException.class)
                .hasMessage("User can only review once.");
    }

    @Test
    void shouldRejectReviewWithoutEnrollment() {
        when(courseService.findEntityById(10)).thenReturn(course);
        when(enrollmentGateway.findByUserIdAndCourseId(2, 10)).thenReturn(Optional.empty());
        when(repository.existsByAuthorIdAndCourseId(2, 10)).thenReturn(false);

        assertThatThrownBy(() -> service.create(student, 10, request))
                .isInstanceOf(CourseReviewNotAllowedException.class)
                .hasMessage("User hasn't enrolled the course.");
    }

    @Test
    void shouldFindCourseReviewsAndResolveAuthors() {
        CourseReview review = CourseReview.builder().id(30).course(course).authorId(2).rating(5).comment("Excellent course").build();
        CourseReviewResponse response = new CourseReviewResponse(30, "student", 5, "Excellent course", LocalDateTime.now());
        PageRequest pageable = PageRequest.of(0, 10);
        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(review), pageable, 1));
        when(userGateway.findUsernamesByIds(Set.of(2))).thenReturn(Map.of(2, "student"));
        when(courseService.findEntityById(10)).thenReturn(course);
        when(enrollmentGateway.existsByUserIdAndCourseId(2, 10)).thenReturn(true);
        when(mapper.toDto(review, "student")).thenReturn(response);

        Page<CourseReviewResponse> result = service.findCourseReviews(student, 10, new CourseReviewFilter(5), pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void shouldDeleteOwnReview() {
        CourseReview review = CourseReview.builder().id(30).course(course).authorId(2).rating(5).build();
        when(repository.findByIdAndCourseId(30, 10)).thenReturn(Optional.of(review));

        service.deleteById(student, 10, 30);

        verify(repository).delete(review);
    }

    @Test
    void shouldRejectDeletingAnotherUsersReview() {
        CourseReview review = CourseReview.builder().id(30).course(course).authorId(3).rating(5).build();
        when(repository.findByIdAndCourseId(30, 10)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> service.deleteById(student, 10, 30))
                .isInstanceOf(ReviewOwnershipException.class)
                .hasMessage("You cannot delete another user's review.");
        verify(repository, never()).delete(any(CourseReview.class));
    }

    @Test
    void shouldThrowWhenDeletingMissingReview() {
        when(repository.findByIdAndCourseId(30, 10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteById(student, 10, 30))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Course Review not found.");
    }
}
