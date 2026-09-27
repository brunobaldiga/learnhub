package org.example.learnhub.enrollment.service;

import org.example.learnhub.enrollment.dto.EnrollmentResponse;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.gateway.dto.CourseSummary;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {
    public EnrollmentResponse toDto(Enrollment enrollment, CourseSummary courseSummary, Integer completedLessons, Integer totalLessons) {
        double percentage = totalLessons == 0 ? 0.0 : (((double) completedLessons / totalLessons) * 100);

        return new EnrollmentResponse(
                enrollment.getId(),
                courseSummary.id(),
                courseSummary.title(),
                courseSummary.creatorUsername(),
                completedLessons,
                totalLessons,
                percentage,
                enrollment.getEnrolledAt()
        );

    }
}
