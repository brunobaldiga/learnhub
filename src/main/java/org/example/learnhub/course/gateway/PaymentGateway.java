package org.example.learnhub.course.gateway;

public interface PaymentGateway {
    Boolean findByUserIdAndCourseId(Integer id, Integer courseId);
}
