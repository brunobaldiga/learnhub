package org.example.learnhub.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.exception.CourseAccessDenied;
import org.example.learnhub.exception.DuplicatePurchaseException;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.gateway.EnrollmentGateway;
import org.example.learnhub.payment.dto.CurrencyType;
import org.example.learnhub.payment.dto.PurchaseResponse;
import org.example.learnhub.payment.entity.Payment;
import org.example.learnhub.payment.repository.PaymentRepository;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final CourseGateway courseGateway;
    private final EnrollmentGateway enrollmentGateway;
    private final PaymentRepository repository;
    private final PaymentMapper mapper;

    @Transactional
    public PurchaseResponse purchase(User user, Integer courseId) {
        Course course = courseGateway.findCourseById(user, courseId);

        if(repository.existsByUserIdAndCourseId(user.getId(), courseId))
            throw new DuplicatePurchaseException("User has already paid for this course.");

        if(user.getId().equals(course.getCreator().getId()) || !course.getStatus().equals(CourseStatus.PUBLIC))
            throw new CourseAccessDenied("Course access denied");

        Payment payment = Payment.builder()
                .userId(user.getId())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .coursePrice(course.getPrice())
                .currency(CurrencyType.USD)
                .build();

        course.setSalesAmount(course.getSalesAmount() + 1);

        payment = repository.save(payment);

        enrollmentGateway.enroll(user, course.getId());

        return mapper.toDto(payment);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseResponse> history(User user, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return repository.findAll(
                PaymentSpecification.filter(
                        user.getId(),
                        startDate,
                        endDate
                ),
                pageable
        ).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public PurchaseResponse findById(User user, Integer paymentId) {
        return repository.findByIdAndUserId(paymentId, user.getId())
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFound("Payment not found"));
    }
}
