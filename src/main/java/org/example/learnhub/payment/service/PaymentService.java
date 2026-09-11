package org.example.learnhub.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.exception.CourseAccessDeniedException;
import org.example.learnhub.exception.DuplicatePurchaseException;
import org.example.learnhub.exception.EntityNotFoundException;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.gateway.CurrencyExchangeGateway;
import org.example.learnhub.gateway.EnrollmentGateway;
import org.example.learnhub.gateway.dto.CourseInfo;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
import org.example.learnhub.payment.dto.PurchaseRequest;
import org.example.learnhub.payment.dto.PurchaseResponse;
import org.example.learnhub.payment.entity.Payment;
import org.example.learnhub.payment.repository.PaymentRepository;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final CourseGateway courseGateway;
    private final EnrollmentGateway enrollmentGateway;
    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final CurrencyExchangeGateway currencyExchangeGateway;

    @Transactional
    public PurchaseResponse purchase(User user, Integer courseId, PurchaseRequest request) {
        CourseInfo course = courseGateway.findById(user.getId(), courseId);

        if(user.getId().equals(course.creatorId()) || !course.status().equals(CourseStatus.PUBLIC))
            throw new CourseAccessDeniedException("Course access denied.");

        if(repository.existsByUserIdAndCourseId(user.getId(), courseId))
            throw new DuplicatePurchaseException("User has already paid for this course.");

        CurrencyCode courseCurrency = course.currency();
        CurrencyCode paymentCurrency = request.currency();

        BigDecimal exchangeRate = currencyExchangeGateway.getExchangeRate(courseCurrency, paymentCurrency);

        BigDecimal paidPrice = course.price()
                .multiply(exchangeRate)
                .setScale(2, RoundingMode.HALF_UP);

        Payment payment = Payment.builder()
                .userId(user.getId())
                .courseId(course.id())
                .courseTitle(course.title())
                .coursePrice(course.price())
                .courseCurrency(courseCurrency)
                .exchangeRate(exchangeRate)
                .paidCurrency(paymentCurrency)
                .paidPrice(paidPrice)
                .build();

        courseGateway.incrementSalesAmount(courseId);

        payment = repository.save(payment);

        enrollmentGateway.enroll(user, course.id());

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
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
    }
}
