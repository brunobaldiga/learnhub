package org.example.learnhub.gateway;

public interface PaymentGateway {
    Boolean existsByUserIdAndCourseId(Integer id, Integer courseId);
}
