package org.example.learnhub.course.controller;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.*;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService service;

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PostMapping()
    public ResponseEntity<CourseResponse> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Validated CourseRequest request
    ) {
        return ResponseEntity.ok(service.create(user, request));
    }

    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<Page<CourseResponse>> search(
            CourseFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.search(filter, pageable));
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page<CourseResponse>> findUserCourses(
            @AuthenticationPrincipal() User user,
            CourseFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findCourses(user, filter, pageable));
    }


    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> findCourseById(
            @AuthenticationPrincipal User user,
            @PathVariable Integer courseId
    ) {
        return ResponseEntity.ok(service.findCourseById(user, courseId));
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PatchMapping("/{courseId}")
    public ResponseEntity<CourseResponse> updateCourseById(
            @AuthenticationPrincipal User user,
            @PathVariable Integer courseId,
            @RequestBody UpdateCourseRequest request
    ) {
        return ResponseEntity.ok(service.updateCourseById(user, courseId, request));
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PostMapping("/{courseId}/sections")
    public ResponseEntity<CourseResponse> createCourseSection(
            @AuthenticationPrincipal User user,
            @PathVariable Integer courseId,
            @RequestBody SectionRequest request
    ) {
        return ResponseEntity.ok(service.createCourseSection(user, courseId, request));
    }
}
