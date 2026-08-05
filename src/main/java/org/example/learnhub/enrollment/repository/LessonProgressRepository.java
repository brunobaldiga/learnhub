package org.example.learnhub.enrollment.repository;

import org.example.learnhub.enrollment.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Integer> {
    Optional<LessonProgress> findByLessonIdAndEnrollmentId(Integer lessonId, Integer enrollmentId);
}
