package org.example.learnhub.payment.service;

import org.example.learnhub.payment.dto.PurchaseResponse;
import org.example.learnhub.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PurchaseResponse toDto(Payment payment) {
        return new PurchaseResponse(
                payment.getId(),
                payment.getCourseId(),
                payment.getCourseTitle(),
                payment.getCoursePrice(),
                payment.getCreatedAt()
        );
    }
}
