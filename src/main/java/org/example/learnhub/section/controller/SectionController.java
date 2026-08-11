package org.example.learnhub.section.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.learnhub.section.dto.LessonResponse;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.dto.LessonRequest;
import org.example.learnhub.section.service.SectionService;
import org.example.learnhub.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@Tag(
        name = "Sections",
        description = "Operations related to course sections and lessons"
)
@SecurityRequirement(name = "bearerAuth")
public class SectionController {
    private final SectionService service;

    @PreAuthorize("hasRole('CREATOR')")
    @PostMapping("/{sectionId}/lessons")
    @Operation(
            summary = "Add lesson to section",
            description = "Creates a new lesson and adds it to the specified section"
    )
    public ResponseEntity<SectionResponse> createLesson(
            @AuthenticationPrincipal User user,
            @PathVariable Integer sectionId,
            @RequestBody LessonRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createLesson(user, sectionId, request));
    }

    @PreAuthorize("hasRole('CREATOR')")
    @DeleteMapping("/{sectionId}/lessons/{lessonId}")
    @Operation(
            summary = "Remove lesson from section",
            description = "Deletes a lesson from the specified section"
    )
    public ResponseEntity<Void> deleteLesson(
            @AuthenticationPrincipal User user,
            @PathVariable Integer sectionId,
            @PathVariable Integer lessonId
    ) {
        service.deleteLesson(user, sectionId, lessonId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{sectionId}/lessons")
    @Operation(
            summary = "List section lessons",
            description = "Returns all lessons belonging to the specified section"
    )
    public ResponseEntity<List<LessonResponse>> findSectionLessons(
            @AuthenticationPrincipal User user,
            @PathVariable Integer sectionId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.findSectionLessons(user, sectionId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/lessons/{lessonId}")
    @Operation(
            summary = "Get lesson details",
            description = "Returns lesson details by its identifier"
    )
    public ResponseEntity<LessonResponse> findLessonById(
            @PathVariable Integer lessonId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.findLessonById(lessonId));
    }


}