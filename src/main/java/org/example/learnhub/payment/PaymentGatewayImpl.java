package org.example.learnhub.payment;

import org.example.learnhub.course.gateway.PaymentGateway;
import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayImpl implements PaymentGateway {
    
    @Override
    public Boolean findByUserIdAndCourseId(Integer id, Integer courseId) {
        return null;
    }
}
