package org.example.learnhub.payment;

import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.exception.CourseAccessDeniedException;
import org.example.learnhub.exception.DuplicatePurchaseException;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.gateway.EnrollmentGateway;
import org.example.learnhub.gateway.dto.CourseInfo;
import org.example.learnhub.payment.dto.CurrencyType;
import org.example.learnhub.payment.dto.PurchaseResponse;
import org.example.learnhub.payment.entity.Payment;
import org.example.learnhub.payment.repository.PaymentRepository;
import org.example.learnhub.payment.service.PaymentMapper;
import org.example.learnhub.payment.service.PaymentService;
import org.example.learnhub.user.dto.RoleType;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private CourseInfo course;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).username("john").roleType(RoleType.USER).build();
        course = new CourseInfo(10, 2, "Java Course", BigDecimal.valueOf(99.99), CourseStatus.PUBLIC);
    }

    @Test
    void shouldPurchaseCourseSuccessfully() {
        Payment payment = Payment.builder()
                .id(1)
                .userId(1)
                .courseId(10)
                .courseTitle("Java Course")
                .coursePrice(course.price())
                .currency(CurrencyType.USD)
                .build();

        PurchaseResponse response = new PurchaseResponse(1, 10, "Java Course", course.price(), LocalDateTime.now());

        when(courseGateway.findById(1, 10)).thenReturn(course);
        when(repository.existsByUserIdAndCourseId(1, 10)).thenReturn(false);
        when(repository.save(any(Payment.class))).thenReturn(payment);
        when(mapper.toDto(payment)).thenReturn(response);

        PurchaseResponse result = service.purchase(user, 10);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(repository).save(captor.capture());
        verify(courseGateway).incrementSalesAmount(10);
        verify(enrollmentGateway).enroll(user, 10);
        assertThat(captor.getValue().getCurrency()).isEqualTo(CurrencyType.USD);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldReturn409WhenCourseWasAlreadyPurchased() {
        when(courseGateway.findById(1, 10)).thenReturn(course);
        when(repository.existsByUserIdAndCourseId(1, 10)).thenReturn(true);

        assertThatThrownBy(() -> service.purchase(user, 10))
                .isInstanceOf(DuplicatePurchaseException.class)
                .hasMessage("User has already paid for this course.");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturn403WhenCreatorPurchasesOwnCourse() {
        CourseInfo ownCourse = new CourseInfo(10, 1, "Java Course", BigDecimal.TEN, CourseStatus.PUBLIC);
        when(courseGateway.findById(1, 10)).thenReturn(ownCourse);
        when(repository.existsByUserIdAndCourseId(1, 10)).thenReturn(false);

        assertThatThrownBy(() -> service.purchase(user, 10)).isInstanceOf(CourseAccessDeniedException.class);
    }

    @Test
    void shouldReturn403WhenCourseIsPrivate() {
        CourseInfo privateCourse = new CourseInfo(10, 2, "Java Course", BigDecimal.TEN, CourseStatus.PRIVATE);
        when(courseGateway.findById(1, 10)).thenReturn(privateCourse);
        when(repository.existsByUserIdAndCourseId(1, 10)).thenReturn(false);

        assertThatThrownBy(() -> service.purchase(user, 10)).isInstanceOf(CourseAccessDeniedException.class);
    }

    @Test
    void shouldReturnPaymentHistorySuccessfully() {
        Payment payment = Payment.builder().id(1).userId(1).courseId(10).courseTitle("Java Course").coursePrice(BigDecimal.TEN).currency(CurrencyType.USD).build();
        PurchaseResponse response = new PurchaseResponse(1, 10, "Java Course", BigDecimal.TEN, LocalDateTime.now());
        Page<Payment> page = new PageImpl<>(List.of(payment), PageRequest.of(0, 10), 1);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(mapper.toDto(payment)).thenReturn(response);

        Page<PurchaseResponse> result = service.history(user, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), PageRequest.of(0, 10));

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void shouldReturnPaymentByIdSuccessfully() {
        Payment payment = Payment.builder().id(1).userId(1).courseId(10).courseTitle("Java Course").coursePrice(BigDecimal.TEN).currency(CurrencyType.USD).build();
        PurchaseResponse response = new PurchaseResponse(1, 10, "Java Course", BigDecimal.TEN, LocalDateTime.now());
        when(repository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(payment));
        when(mapper.toDto(payment)).thenReturn(response);

        assertThat(service.findById(user, 1)).isEqualTo(response);
    }

    @Test
    void shouldReturn404WhenPaymentDoesNotBelongToUser() {
        when(repository.findByIdAndUserId(1, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(user, 1))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Payment not found");
    }
}