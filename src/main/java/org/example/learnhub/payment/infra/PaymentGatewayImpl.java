package org.example.learnhub.payment.infra;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.gateway.PaymentGateway;
import org.example.learnhub.payment.repository.PaymentRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentGatewayImpl implements PaymentGateway {
    private final PaymentRepository repository;

    @Override
    public Boolean existsByUserIdAndCourseId(Integer userId, Integer courseId) {
        return repository.existsByUserIdAndCourseId(userId, courseId);
    }
}
