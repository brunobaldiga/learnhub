package org.example.learnhub.course.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.CourseReviewFilter;
import org.example.learnhub.course.dto.CourseReviewRequest;
import org.example.learnhub.course.dto.CourseReviewResponse;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseReview;
import org.example.learnhub.course.repository.CourseReviewRepository;
import org.example.learnhub.course.repository.CourseReviewSpecs;
import org.example.learnhub.exception.CourseReviewNotAllowedException;
import org.example.learnhub.exception.DuplicateReviewException;
import org.example.learnhub.exception.ReviewOwnershipException;
import org.example.learnhub.exception.SelfReviewNotAllowedException;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.gateway.PaymentGateway;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseReviewService {
    private final CourseReviewRepository repository;
    private final CourseGateway courseGateway;
    private final PaymentGateway paymentGateway;
    private final CourseReviewMapper mapper;

    @Transactional
    public CourseReviewResponse createCourseReview(User user, Integer courseId, CourseReviewRequest request) {
        Course course = courseGateway.findCourseById(user, courseId);

        if (course.getCreator().getId().equals(user.getId()))
            throw new SelfReviewNotAllowedException("Course creator cannot review its own course.");

        if (repository.existsByAuthorIdAndCourseId(user.getId(), courseId))
            throw new DuplicateReviewException("User can only review once.");

        if (!paymentGateway.existsByUserIdAndCourseId(user.getId(), courseId)) {
            throw new CourseReviewNotAllowedException("User hasn't bought the course.");
        }

        CourseReview courseReview = mapper.toCourseReview(user, course, request);

        course.addReview(request.rating());

        repository.save(courseReview);

        return mapper.toDto(courseReview);
    }

    public Page<CourseReviewResponse> findCourseReviews(CourseReviewFilter filter, Pageable pageable) {
        Specification<CourseReview> specification = Specification
                .where(CourseReviewSpecs.withFilter(filter));

        return repository.findAll(specification, pageable)
                .map(mapper::toDto);
    }

    public void deleteReviewById(User user, Integer courseId, Integer courseReviewId) {
        CourseReview courseReview = repository.findByIdAndCourseId(courseReviewId, courseId);

        if (!courseReview.getAuthor().getId().equals(user.getId()))
            throw new ReviewOwnershipException("User is not the author of the review");

        repository.delete(courseReview);
    }
}
