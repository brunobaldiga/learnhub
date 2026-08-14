package org.example.learnhub.gateway;

import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.user.entity.User;

import java.util.Optional;

public interface EnrollmentGateway {
    void enroll(User user, Integer id);

    Optional<Enrollment> findEnrollmentByUserIdAndCourseId(Integer userId, Integer courseId);
}
