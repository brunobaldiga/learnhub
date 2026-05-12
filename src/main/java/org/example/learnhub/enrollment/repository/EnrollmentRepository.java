package org.example.learnhub.enrollment.repository;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    Optional<Enrollment> findByUserAndCourse(User user, Course course);

    Page<Enrollment> findByEnrollmentsUserId(Integer userId, Pageable pageable);
}
