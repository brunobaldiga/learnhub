package org.example.learnhub.sections.repository;

import org.example.learnhub.sections.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Integer> {
    Optional<Section> findByIdAndUserId(Integer sectionId, Integer id);
}
