package org.example.learnhub.enrollment.service;

import org.example.learnhub.enrollment.dto.EnrollmentResponse;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {
    public EnrollmentResponse toDto(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getCourse().getCreator().getUsername(),
                enrollment.getCompletedLessons(),
                enrollment.getTotalLessons(),
                (((double) enrollment.getCompletedLessons() / enrollment.getTotalLessons()) * 100),
                enrollment.getEnrolledAt()
        );

    }
}
