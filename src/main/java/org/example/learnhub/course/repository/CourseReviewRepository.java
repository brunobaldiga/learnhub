package org.example.learnhub.course.repository;

import org.example.learnhub.course.entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Integer> {
    boolean existsByUserIdAndCourseId(Integer id, Integer courseId);
}
