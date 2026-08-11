package org.example.learnhub.gateway;

import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.user.entity.User;

public interface EnrollmentGateway {
    void enroll(User user, Integer id);
    Enrollment findEnrollmentByUserId(Integer userId);
}
