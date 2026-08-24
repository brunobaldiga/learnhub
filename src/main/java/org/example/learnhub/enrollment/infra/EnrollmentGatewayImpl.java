package org.example.learnhub.enrollment.infra;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.enrollment.service.EnrollmentService;
import org.example.learnhub.gateway.EnrollmentGateway;
import org.example.learnhub.gateway.dto.EnrollmentInfo;
import org.example.learnhub.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EnrollmentGatewayImpl implements EnrollmentGateway {
    private final EnrollmentService service;

    @Override
    public void enroll(User user, Integer id) {
        service.enroll(user, id);
    }

    @Override
    public Optional<EnrollmentInfo> findEnrollmentByUserIdAndCourseId(Integer userId, Integer courseId) {
        Enrollment enrollment = service.findEnrollmentEntityByUserIdAndCourseId(userId, courseId);

        return new EnrollmentInfo(
                enrollment.getId(),
                enrollment.getUser().getId(),
                enrollment.getCourse().getId(),
                enrollment.getEnrolledAt()
        );
    }
}
