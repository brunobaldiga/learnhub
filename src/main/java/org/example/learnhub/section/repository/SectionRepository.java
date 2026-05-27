package org.example.learnhub.section.repository;

import org.example.learnhub.section.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Integer> {
    Optional<Section> findByIdAndCourseCreatorId(Integer sectionId, Integer id);
}
