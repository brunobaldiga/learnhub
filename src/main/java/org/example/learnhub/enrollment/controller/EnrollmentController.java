package org.example.learnhub.enrollment.controller;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.CourseResponse;
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
public class EnrollmentController {
    private final EnrollmentService service;

    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @PostMapping("/{courseId}")
    public ResponseEntity<Void> enroll(
            @AuthenticationPrincipal User user,
            @PathVariable Integer courseId
    ) {
        service.enroll(user, courseId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @GetMapping()
    public ResponseEntity<Page<EnrollmentResponse>> getUserEnrollments(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getEnrolledCourses(user.getId(), page, size));
    }

    @GetMapping("/{enrollmentId")
    public ResponseEntity<EnrollmentResponse> getEnrollment
}
