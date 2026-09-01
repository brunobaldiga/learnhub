package org.example.learnhub.payment;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.payment.dto.CurrencyType;
import org.example.learnhub.payment.entity.Payment;
import org.example.learnhub.payment.repository.PaymentRepository;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.learnhub.payment.service.PaymentSpecification.filter;

@Testcontainers
@DataJpaTest
public class PaymentRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository repository;

    private User user;
    private Integer courseId;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .roleType(RoleType.USER)
                .build();

        entityManager.persist(user);

        Course course = Course.builder()
                .creatorId(user.getId())
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .averageRating(0.0)
                .build();

        entityManager.persist(course);
        entityManager.flush();

        courseId = course.getId();
    }

    @Test
    void shouldReturnPaymentByIdAndUserId() {
        Payment payment = Payment.builder()
                .userId(user.getId())
                .courseId(courseId)
                .courseTitle("Java Course")
                .coursePrice(BigDecimal.TEN)
                .currency(CurrencyType.BRL)
                .build();

        payment = entityManager.persist(payment);

        entityManager.flush();
        entityManager.clear();

        Optional<Payment> result = repository.findByIdAndUserId(payment.getId(), user.getId());

        assertThat(result.isPresent());
        assertThat(result.get().getUserId()).isEqualTo(courseId);
    }

    @Test
    void shouldReturnEmptyWhenPaymentDoesNotBelongToUser() {
        User other = User.builder()
                .username("other")
                .email("other@example.com")
                .fullName("Other User")
                .password("password")
                .roleType(RoleType.USER)
                .build();

        Payment payment = Payment.builder()
                .userId(1)
                .courseId(1)
                .courseTitle("Java Course")
                .coursePrice(BigDecimal.TEN)
                .currency(CurrencyType.BRL)
                .build();

        other = entityManager.persist(other);
        payment = entityManager.persist(payment);

        entityManager.flush();
        entityManager.clear();

        Optional<Payment> result = repository.findByIdAndUserId(payment.getId(), other.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenUserHasPaidForCourse() {
        entityManager.persist(Payment.builder()
                .userId(user.getId())
                .courseId(courseId)
                .courseTitle("Java Course")
                .coursePrice(BigDecimal.TEN)
                .currency(CurrencyType.BRL)
                .build()
        );

        entityManager.flush();
        entityManager.clear();

        boolean result = repository.existsByUserIdAndCourseId(user.getId(), courseId);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserHasNotPaidForCourse() {
        boolean result = repository.existsByUserIdAndCourseId(user.getId(), courseId);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnPaymentsWithinDataRange() {
        entityManager.persist(Payment.builder()
                .userId(user.getId())
                .courseId(courseId)
                .courseTitle("Java Course")
                .coursePrice(BigDecimal.TEN)
                .currency(CurrencyType.BRL)
                .build()
        );

        Page<Payment> result = repository.findAll(
                filter(user.getId(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
    }
}
