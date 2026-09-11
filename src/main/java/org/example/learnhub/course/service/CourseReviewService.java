package org.example.learnhub.course.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.CourseReviewFilter;
import org.example.learnhub.course.dto.CourseReviewRequest;
import org.example.learnhub.course.dto.CourseReviewResponse;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseReview;
import org.example.learnhub.course.repository.CourseReviewRepository;
import org.example.learnhub.course.repository.CourseReviewSpecs;
import org.example.learnhub.exception.*;
import org.example.learnhub.gateway.EnrollmentGateway;
import org.example.learnhub.gateway.UserGateway;
import org.example.learnhub.gateway.dto.EnrollmentInfo;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseReviewService {
    private final CourseReviewRepository repository;
    private final CourseService courseService;
    private final EnrollmentGateway enrollmentGateway;
    private final CourseReviewMapper mapper;
    private final UserGateway userGateway;

    @Transactional
    public CourseReviewResponse create(User user, Integer courseId, CourseReviewRequest request) {
        Course course = courseService.findCourseEntityById(courseId);
        Optional<EnrollmentInfo> enrollment = enrollmentGateway.findByUserIdAndCourseId(user.getId(), courseId);

        if(course.getCreatorId().equals(user.getId()))
            throw new SelfReviewNotAllowedException("Course creator cannot review its own course.");

        if(repository.existsByAuthorIdAndCourseId(user.getId(), courseId))
            throw new DuplicateReviewException("User can only review once.");

        if(enrollment.isEmpty()) {
            throw new CourseReviewNotAllowedException("User hasn't enrolled the course.");
        }

        CourseReview courseReview = mapper.toCourseReview(user, course, request);

        courseService.recordReview(courseId, request.rating());

        repository.save(courseReview);

        return mapper.toDto(courseReview, user.getUsername());
    }

    @Transactional(readOnly = true)
    public Page<CourseReviewResponse> findCourseReviews(Integer courseId, CourseReviewFilter filter, Pageable pageable) {
        Specification<CourseReview> specification = Specification
                .where(CourseReviewSpecs.withFilter(filter))
                .and(CourseReviewSpecs.withCourseId(courseId));

        Page<CourseReview> courseReviews = repository.findAll(specification, pageable);

        Set<Integer> authorIds = courseReviews.getContent().stream()
                .map(CourseReview::getAuthorId)
                .collect(Collectors.toSet());

        Map<Integer, String> usernamesByCreatorId = userGateway.findUsernamesByIds(authorIds);

        return courseReviews.map(courseReview -> mapper.toDto(courseReview, usernamesByCreatorId.get(courseReview.getAuthorId())));
    }

    @Transactional
    public void deleteById(User user, Integer courseId, Integer courseReviewId) {
        CourseReview courseReview = repository.findByIdAndCourseId(courseReviewId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course Review not found."));

        if(!courseReview.getAuthorId().equals(user.getId()))
            throw new ReviewOwnershipException("You cannot delete another user's review.");

        repository.delete(courseReview);
        courseReview.getCourse().removeReview(courseReview.getRating());
    }
}
