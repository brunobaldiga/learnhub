package org.example.learnhub.course.repository;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer>, JpaSpecificationExecutor<Course> {
    @Query("""
        SELECT COUNT(v)
        FROM Video v
        WHERE v.section.course.id = :courseId
    """)
    Integer countVideosByCourseId(Integer courseId);


    Optional<Course> findByIdAndStatus(Integer id, CourseStatus status);

    Optional<Course> findByIdAndCreatorId(Integer courseId, Integer id);
}
