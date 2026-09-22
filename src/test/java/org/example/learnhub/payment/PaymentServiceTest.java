package org.example.learnhub.payment;

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
import org.example.learnhub.payment.service.PaymentMapper;
import org.example.learnhub.payment.service.PaymentService;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    CourseGateway courseGateway;
    @Mock
    EnrollmentGateway enrollmentGateway;
    @Mock
    PaymentRepository repository;
    @Mock
    PaymentMapper mapper;
    @Mock
    CurrencyExchangeGateway currencyExchangeGateway;

    @InjectMocks
    PaymentService service;

    private User user;
    private CourseInfo course;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).username("john").build();
        course = new CourseInfo(
                10, 2, "Java Course", BigDecimal.valueOf(99.99), CurrencyCode.USD, CourseStatus.PUBLIC
        );
    }

    @Test
    void shouldPurchaseCourseUsingRequestedCurrency() {
        PurchaseRequest request = new PurchaseRequest(CurrencyCode.BRL);
        BigDecimal rate = BigDecimal.valueOf(5.25);
        Payment saved = Payment.builder()
                .id(1)
                .userId(1)
                .courseId(10)
                .courseTitle("Java Course")
                .coursePrice(course.price())
                .courseCurrency(CurrencyCode.USD)
                .exchangeRate(rate)
                .paidCurrency(CurrencyCode.BRL)
                .paidPrice(BigDecimal.valueOf(524.95))
                .createdAt(LocalDateTime.now())
                .build();
        PurchaseResponse response = new PurchaseResponse(
                1, 10, "Java Course", course.price(), CurrencyCode.USD,
                rate, CurrencyCode.BRL, BigDecimal.valueOf(524.95), saved.getCreatedAt()
        );

        when(courseGateway.findById(10)).thenReturn(course);
        when(repository.existsByUserIdAndCourseId(1, 10)).thenReturn(false);
        when(currencyExchangeGateway.getExchangeRate(CurrencyCode.USD, CurrencyCode.BRL)).thenReturn(rate);
        when(repository.save(any(Payment.class))).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(response);

        assertThat(service.purchase(user, 10, request)).isEqualTo(response);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(repository).save(captor.capture());
        Payment payment = captor.getValue();
        assertThat(payment.getCourseCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(payment.getPaidCurrency()).isEqualTo(CurrencyCode.BRL);
        assertThat(payment.getExchangeRate()).isEqualByComparingTo(rate);
        assertThat(payment.getPaidPrice()).isEqualByComparingTo("524.95");
        verify(courseGateway).incrementSalesAmount(10);
        verify(enrollmentGateway).enroll(user.getId(), 10);
    }

    @Test
    void shouldRejectDuplicatePurchase() {
        when(courseGateway.findById(10)).thenReturn(course);
        when(repository.existsByUserIdAndCourseId(1, 10)).thenReturn(true);

        assertThatThrownBy(() -> service.purchase(user, 10, new PurchaseRequest(CurrencyCode.USD)))
                .isInstanceOf(DuplicatePurchaseException.class)
                .hasMessage("User has already paid for this course.");
        verify(repository, never()).save(any());
        verify(enrollmentGateway, never()).enroll(any(), any());
    }

    @Test
    void shouldRejectCreatorPurchasingOwnCourse() {
        CourseInfo ownCourse = new CourseInfo(10, 1, "Java", BigDecimal.TEN, CurrencyCode.USD, CourseStatus.PUBLIC);
        when(courseGateway.findById(10)).thenReturn(ownCourse);

        assertThatThrownBy(() -> service.purchase(user, 10, new PurchaseRequest(CurrencyCode.USD)))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessage("Course access denied.");
        verify(repository, never()).existsByUserIdAndCourseId(any(), any());
    }

    @Test
    void shouldRejectPrivateCoursePurchase() {
        CourseInfo privateCourse = new CourseInfo(10, 2, "Java", BigDecimal.TEN, CurrencyCode.USD, CourseStatus.PRIVATE);
        when(courseGateway.findById(10)).thenReturn(privateCourse);

        assertThatThrownBy(() -> service.purchase(user, 10, new PurchaseRequest(CurrencyCode.USD)))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessage("Course access denied.");
    }

    @Test
    void shouldReturnPaymentHistory() {
        Payment payment = Payment.builder().id(1).userId(1).courseId(10).build();
        PurchaseResponse response = new PurchaseResponse(
                1, 10, "Java", BigDecimal.TEN, CurrencyCode.USD,
                BigDecimal.ONE, CurrencyCode.USD, BigDecimal.TEN, LocalDateTime.now()
        );
        PageRequest pageable = PageRequest.of(0, 10);
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(payment), pageable, 1));
        when(mapper.toDto(payment)).thenReturn(response);

        Page<PurchaseResponse> result = service.history(
                user, LocalDate.now().minusDays(30), LocalDate.now(), pageable
        );

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void shouldFindPaymentByIdForOwner() {
        Payment payment = Payment.builder().id(1).userId(1).courseId(10).build();
        PurchaseResponse response = new PurchaseResponse(
                1, 10, "Java", BigDecimal.TEN, CurrencyCode.USD,
                BigDecimal.ONE, CurrencyCode.USD, BigDecimal.TEN, LocalDateTime.now()
        );
        when(repository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(payment));
        when(mapper.toDto(payment)).thenReturn(response);

        assertThat(service.findById(user, 1)).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenPaymentDoesNotBelongToUser() {
        when(repository.findByIdAndUserId(1, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(user, 1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Payment not found");
    }
}
