package org.example.learnhub.course.repository;

import org.example.learnhub.course.dto.CourseReviewFilter;
import org.example.learnhub.course.entity.CourseReview;
import org.springframework.data.jpa.domain.Specification;

public class CourseReviewSpecs {
    public static Specification<CourseReview> withFilter(CourseReviewFilter filter) {
        return Specification
                .where(hasRating(filter.rating()));
    }

    private static Specification<CourseReview> hasRating(Integer rating) {
        return (root, query, criteriaBuilder) -> {
            if(rating == null) return null;
            return criteriaBuilder.equal(root.get("rating"), rating);
        };
    }

    public static Specification<CourseReview> withCourseId(Integer courseId) {
        return (root, query, criteriaBuilder) -> {
            if(courseId == null) return null;
            return criteriaBuilder.equal(root.get("course").get("id"), courseId);
        };
    }
}
