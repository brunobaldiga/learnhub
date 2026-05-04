package org.example.learnhub.course.repository;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseProgress;
import org.example.learnhub.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, Integer> {
    Optional<CourseProgress> findByUserAndCourse(User user, Course course);

    @Query("""
        SELECT COUNT(v)
        FROM Video v
        WHERE v.section.course.id = :courseId        
    """)
    Integer countVideosByCourseId(Integer courseId);

    Optional<CourseProgress> findByUserIdAndCourseId(Integer id, Integer courseId);
}
