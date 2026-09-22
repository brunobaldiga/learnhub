package org.example.learnhub.gateway;

import org.example.learnhub.gateway.dto.EnrollmentInfo;

import java.util.Optional;

public interface EnrollmentGateway {
    void enroll(Integer userId, Integer id);

    Optional<EnrollmentInfo> findByUserIdAndCourseId(Integer userId, Integer courseId);

    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);
}
