package org.example.learnhub.payment;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.gateway.EnrollmentGateway;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.payment.dto.CurrencyType;
import org.example.learnhub.payment.dto.PurchaseResponse;
import org.example.learnhub.payment.entity.Payment;
import org.example.learnhub.payment.repository.PaymentRepository;
import org.example.learnhub.payment.service.PaymentMapper;
import org.example.learnhub.payment.service.PaymentService;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private CourseGateway courseGateway;

    @Mock
    private EnrollmentGateway enrollmentGateway;

    @Mock
    private PaymentRepository repository;

    @Mock
    private PaymentMapper mapper;

    @InjectMocks
    private PaymentService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .username("John")
                .build();
    }

    @Test
    void shouldPurchaseCourseSuccessfully() {
        Course course = Course.builder().id(1).title("Java Course").price(BigDecimal.valueOf(99.99)).build();

        Payment payment = Payment.builder()
                .id(1)
                .userId(user.getId())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .coursePrice(course.getPrice())
                .amount(course.getPrice())
                .currency(CurrencyType.USD)
                .build();

        PurchaseResponse response = new PurchaseResponse(
                1,
                1,
                "Java Course",
                BigDecimal.valueOf(99.99),
                LocalDateTime.now()
        );

        when(courseGateway.findCourseById(user, 1)).thenReturn(course);
        when(repository.save(any())).thenReturn(payment);
        when(mapper.toDto(payment)).thenReturn(response);

        PurchaseResponse result = service.purchase(user, 1);

        verify(enrollmentGateway).enroll(user, course.getId());
        verify(repository).save(any(Payment.class));

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldReturnPaymentHistory() {
        Payment payment = Payment.builder()
                .id(1)
                .courseId(1)
                .courseTitle("Java Course")
                .coursePrice(BigDecimal.valueOf(99.99))
                .build();

        PurchaseResponse response = new PurchaseResponse(
                1,
                1,
                "Java Course",
                BigDecimal.valueOf(99.99),
                LocalDateTime.now()
        );

        Page<Payment> payments = new PageImpl<>(
                List.of(payment),
                PageRequest.of(0, 10),
                1
        );

        when(repository.findByUserIdAndDateRange(
                any(),
                any(),
                any(),
                any()
        )).thenReturn(payments);

        when(mapper.toDto(payment)).thenReturn(response);

        Page<PurchaseResponse> result = service.history(
                user,
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent().get(0)).isEqualTo(response);
    }

    @Test
    void shouldReturnPaymentWhenPaymentExists() {
        Payment payment = Payment.builder()
                .id(1)
                .courseId(1)
                .courseTitle("Java Course")
                .coursePrice(BigDecimal.valueOf(99.99))
                .build();

        PurchaseResponse response = new PurchaseResponse(
                1,
                1,
                "Java Course",
                BigDecimal.valueOf(99.99),
                LocalDateTime.now()
        );

        when(repository.findByIdAndUserId(user.getId(), 1))
                .thenReturn(Optional.of(payment));

        when(mapper.toDto(payment)).thenReturn(response);

        PurchaseResponse result = service.findById(user, 1);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldReturn404WhenPaymentNotFound() {
        when(repository.findByIdAndUserId(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(user, 1))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Payment not found");
    }

    @Test
    void shouldReturnTrueWhenPaymentExistsForCourse() {
        when(repository.existsByUserIdAndCourseId(1, 1))
                .thenReturn(true);

        boolean result = service.existsByUserIdAndCourseId(1, 1);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPaymentDoesNotExistForCourse() {
        when(repository.existsByUserIdAndCourseId(1, 1))
                .thenReturn(false);

        boolean result = service.existsByUserIdAndCourseId(1, 1);

        assertThat(result).isFalse();
    }
}