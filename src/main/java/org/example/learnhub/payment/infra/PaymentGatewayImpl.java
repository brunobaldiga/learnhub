package org.example.learnhub.payment.infra;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.gateway.PaymentGateway;
import org.example.learnhub.payment.service.PaymentService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentGatewayImpl implements PaymentGateway {
    private final PaymentService service;

    @Override
    public Boolean existsByUserIdAndCourseId(Integer id, Integer courseId) {
        return service.existsByUserIdAndCourseId(id, courseId);
    }
}
