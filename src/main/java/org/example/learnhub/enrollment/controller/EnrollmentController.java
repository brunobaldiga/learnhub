package org.example.learnhub.enrollment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.learnhub.enrollment.dto.EnrollmentResponse;
import org.example.learnhub.enrollment.service.EnrollmentService;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(
        name = "Enrollments",
        description = "Operations related to course enrollments"
)
public class EnrollmentController {

    private final EnrollmentService service;

    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @GetMapping
    @Operation(
            summary = "List user enrollments",
            description = "Returns a paginated list of courses the authenticated user is enrolled in"
    )
    public ResponseEntity<Page<EnrollmentResponse>> findUserEnrollments(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                service.findEnrolledCourses(
                        user.getId(),
                        page,
                        size
                )
        );
    }

    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @GetMapping("/{enrollmentId}")
    @Operation(
            summary = "Get enrollment details",
            description = "Returns details of a specific enrollment belonging to the authenticated user"
    )
    public ResponseEntity<EnrollmentResponse> findEnrollmentById(
            @AuthenticationPrincipal User user,
            @PathVariable Integer enrollmentId
    ) {
        return ResponseEntity.ok(
                service.findEnrollmentById(
                        user.getId(),
                        enrollmentId
                )
        );
    }
}