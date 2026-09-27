package org.example.learnhub.enrollment.repository;

import org.example.learnhub.enrollment.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    Page<Enrollment> findByUserId(Integer userId, Pageable pageable);

    Optional<Enrollment> findByIdAndUserId(Integer enrollmentId, Integer userId);

    Optional<Enrollment> findByUserIdAndCourseId(Integer userId, Integer courseId);

    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);
}
