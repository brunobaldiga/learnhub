package org.example.learnhub.section.infra;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.exception.CourseAccessDeniedException;
import org.example.learnhub.exception.EntityNotFoundException;
import org.example.learnhub.gateway.SectionGateway;
import org.example.learnhub.gateway.dto.SectionInfo;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.repository.SectionRepository;
import org.example.learnhub.section.service.SectionMapper;
import org.example.learnhub.section.service.SectionService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SectionGatewayImpl implements SectionGateway {
    private final SectionMapper mapper;
    private final SectionRepository repository;

    @Override
    public SectionInfo create(SectionRequest request, Integer courseId) {
        Section section = mapper.toSection(request, courseId);

        return mapper.toSectionInfo(repository.save(section));
    }

    @Override
    public SectionResponse update(Integer sectionId, SectionRequest request) {
        Section section = repository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found."));

        section.setTitle(request.title());
        section.setPosition(request.position());
        Section saved = repository.save(section);

        return mapper.toDto(saved);
    }

    @Override
    public void delete(Integer sectionId) {
        Section section = repository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        repository.delete(section);
    }

    @Override
    public long countSectionsByCourseId(Integer courseId) {
        return repository.countByCourseId(courseId);
    }

    @Override
    public List<SectionResponse> findAllByCourseId(Integer courseId) {
        return repository.findAllByCourseIdOrderByPositionAsc(courseId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public SectionInfo findById(Integer sectionId) {
        return mapper.toSectionInfo(
                repository.findById(sectionId)
                        .orElseThrow(() -> new EntityNotFoundException("Section not found."))
        );
    }
}
