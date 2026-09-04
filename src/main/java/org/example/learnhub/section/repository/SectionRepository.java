package org.example.learnhub.section.repository;

import org.example.learnhub.section.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Integer> {
    @Query("select coalesce(sum(lesson.duration), 0) from Lesson lesson where lesson.section.courseId = :courseId")
    Integer calculateDurationByCourseId(Integer courseId);
}
