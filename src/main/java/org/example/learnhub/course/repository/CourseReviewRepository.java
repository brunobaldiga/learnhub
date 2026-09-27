package org.example.learnhub.course.repository;

import org.example.learnhub.course.entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Integer>, JpaSpecificationExecutor<CourseReview> {
    boolean existsByAuthorIdAndCourseId(Integer id, Integer courseId);

    Optional<CourseReview> findByIdAndCourseId(Integer reviewId, Integer courseId);
}
