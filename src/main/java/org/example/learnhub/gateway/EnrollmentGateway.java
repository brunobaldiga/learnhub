package org.example.learnhub.gateway;

import org.example.learnhub.gateway.dto.EnrollmentInfo;
import org.example.learnhub.user.entity.User;

import java.util.Optional;

public interface EnrollmentGateway {
    void enroll(User user, Integer id);

    Optional<EnrollmentInfo> findByUserIdAndCourseId(Integer userId, Integer courseId);

    boolean existsByUserIdAndCourseId(Integer id, Integer id1);
}
