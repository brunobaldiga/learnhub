package org.example.learnhub.payment.gateway;


import org.example.learnhub.user.entity.User;

public interface EnrollmentGateway {
    void enroll(User user, Integer id);
}
