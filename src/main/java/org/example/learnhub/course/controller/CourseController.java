package org.example.learnhub.course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.*;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.service.CourseReviewService;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(
        name = "Courses",
        description = "Operations related to course management"
)
public class CourseController {
    private final CourseService service;
    private final CourseReviewService courseReviewService;

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create a course",
            description = "Creates a new course for the authenticated creator"
    )
    public ResponseEntity<CourseResponse> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Validated CourseRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(user, request));
    }

    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @GetMapping("/search")
    @Operation(
            summary = "Search courses",
            description = "Searches courses using filters and pagination"
    )
    public ResponseEntity<Page<CourseResponse>> search(
            CourseFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.search(filter, pageable));
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @GetMapping
    @Operation(
            summary = "List creator courses",
            description = "Returns all courses owned by the authenticated creator"
    )
    public ResponseEntity<Page<CourseResponse>> findUserCourses(
            @AuthenticationPrincipal User user,
            CourseFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findCourses(user, filter, pageable));
    }

    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @GetMapping("/{courseId}")
    @Operation(
            summary = "Get course details",
            description = "Returns course details by its identifier"
    )
    public ResponseEntity<CourseResponse> findCourseById(
            @AuthenticationPrincipal User user,
            @PathVariable Integer courseId
    ) {
        return ResponseEntity.ok(service.findCourseById(user, courseId));
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PatchMapping("/{courseId}")
    @Operation(
            summary = "Update course",
            description = "Updates an existing course"
    )
    public ResponseEntity<CourseResponse> updateCourseById(
            @AuthenticationPrincipal User user,
            @PathVariable Integer courseId,
            @RequestBody @Validated UpdateCourseRequest request
    ) {
        return ResponseEntity.ok(service.updateCourseById(user, courseId, request));
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PostMapping("/{courseId}/sections")
    @Operation(
            summary = "Create section",
            description = "Creates a new section for a course"
    )
    public ResponseEntity<SectionResponse> createCourseSection(
            @AuthenticationPrincipal User user,
            @PathVariable Integer courseId,
            @RequestBody @Validated SectionRequest request
    ) {
        return ResponseEntity.ok(service.createCourseSection(user, courseId, request));
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PutMapping("/{courseId}/section/{sectionId}")
    @Operation(
            summary = "Update section",
            description = "Updates an existing section from a course"
    )
    public ResponseEntity<SectionResponse> updateCourseSection(
            @AuthenticationPrincipal User user,
            @PathVariable Integer sectionId,
            @RequestBody @Validated SectionRequest request
    ) {
        return ResponseEntity.ok(service.updateCourseSection(user, sectionId, request));
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @DeleteMapping("/{courseId}/section/{sectionId}")
    @Operation(
            summary = "Delete section",
            description = "Deletes an existing section from a course"
    )
    public ResponseEntity<Void> deleteCourseSection(
            @AuthenticationPrincipal User user,
            @PathVariable Integer sectionId
    ) {
        service.deleteCourseSection(user, sectionId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @GetMapping("/{courseId}/sections")
    @Operation(
            summary = "List course sections",
            description = "Returns all sections belonging to a course"
    )
    public ResponseEntity<List<SectionResponse>> findCourseSections(
            @AuthenticationPrincipal User user,
            @PathVariable Integer courseId
    ) {
        return ResponseEntity.ok(service.findCourseSection(user, courseId));
    }

    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @PostMapping("/{courseId}/reviews")
    @Operation(
            summary = "Write a course review",
            description = "Allow user who bought the course to write a review"
    )
    public ResponseEntity<CourseReviewResponse> createCourseReview(
            @AuthenticationPrincipal User user,
            @PathVariable Integer courseId,
            @RequestBody @Valid CourseReviewRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseReviewService.createCourseReview(user, courseId, request)));
    }
}