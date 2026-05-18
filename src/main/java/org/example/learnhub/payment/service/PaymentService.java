package org.example.learnhub.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.enrollment.gateway.CourseGateway;
import org.example.learnhub.payment.dto.CurrencyType;
import org.example.learnhub.payment.dto.PurchaseResponse;
import org.example.learnhub.payment.entity.Payment;
import org.example.learnhub.payment.gateway.EnrollmentGateway;
import org.example.learnhub.payment.repository.PaymentRepository;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final CourseGateway courseGateway;
    private final EnrollmentGateway enrollmentGateway;
    private final PaymentRepository repository;
    private final PaymentMapper mapper;

    public PurchaseResponse purchase(User user, Integer courseId) {
        Course course = courseGateway.findCourseById(user, courseId);

        enrollmentGateway.enroll(user, course.getId());

        Payment payment = Payment.builder()
                .userId(user.getId())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .coursePrice(course.getPrice())
                .amount(course.getPrice())
                .currency(CurrencyType.USD)
                .build();

        payment = repository.save(payment);

        return mapper.toDto(payment);
    }


    public Page<PurchaseResponse> history(User user, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return repository.findByUserIdAndDateRange(user.getId(), startDate, endDate, pageable)
                .map(mapper::toDto);
    }

    public PurchaseResponse findById(User user, Integer paymentId) {
        return repository.findByIdAndUserId(user.getId(), paymentId)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException(String.format("Payment with ID %d not found.", paymentId)));
    }
}
