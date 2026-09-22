package org.example.learnhub.enrollment.infra;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.enrollment.repository.EnrollmentRepository;
import org.example.learnhub.exception.UserAlreadyEnrolledException;
import org.example.learnhub.gateway.EnrollmentGateway;
import org.example.learnhub.gateway.dto.EnrollmentInfo;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EnrollmentGatewayImpl implements EnrollmentGateway {
    private final EnrollmentRepository repository;

    @Override
    public void enroll(Integer userId, Integer courseId) {
        if(repository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new UserAlreadyEnrolledException(
                    "User is already enrolled."
            );
        }

        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .courseId(courseId)
                .build();

        repository.save(enrollment);
    }

    @Override
    public boolean existsByUserIdAndCourseId(Integer userId, Integer courseId) {
        return repository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public Optional<EnrollmentInfo> findByUserIdAndCourseId(Integer userId, Integer courseId) {
        return repository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> new EnrollmentInfo(
                        enrollment.getId(),
                        enrollment.getUserId(),
                        enrollment.getCourseId(),
                        enrollment.getEnrolledAt()
                ));
    }


}
