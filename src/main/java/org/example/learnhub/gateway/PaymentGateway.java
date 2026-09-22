package org.example.learnhub.gateway;

public interface PaymentGateway {
    Boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);
}
