package org.example.learnhub.section.repository;

import org.example.learnhub.section.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    Optional<Lesson> findByEnrollmentId(Integer enrollmentId);
}
