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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
    private Course course;

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

        course = Course.builder()
                .creator(user)
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(BigDecimal.TEN)
                .averageRating(0.0)
                .build();

        entityManager.persist(course);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldReturnPaymentByIdAndUserId() {
        Payment payment = Payment.builder()
                .userId(1)
                .courseId(1)
                .courseTitle("Java Course")
                .coursePrice(BigDecimal.TEN)
                .currency(CurrencyType.BRL)
                .build();

        entityManager.persist(payment);

        entityManager.flush();
        entityManager.clear();

        Optional<Payment> result = repository.findByIdAndUserId(payment.getId(), 1);

        assertThat(result.isPresent());
        assertThat(result.get().getId()).isEqualTo(payment.getId());
        assertThat(result.get().getUserId()).isEqualTo(1);
    }

    @Test
    void shouldReturnEmptyWhenPaymentDoesNotBelongToUser() {
        Payment payment = Payment.builder()
                .userId(1)
                .courseId(1)
                .courseTitle("Java Course")
                .coursePrice(BigDecimal.TEN)
                .currency(CurrencyType.BRL)
                .build();

        entityManager.persist(payment);

        entityManager.flush();
        entityManager.clear();

        Optional<Payment> result = repository.findByIdAndUserId(payment.getId(), 2);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenUserHasPaidForCourse() {
        Payment payment = Payment.builder()
                .userId(1)
                .courseId(1)
                .courseTitle("Java Course")
                .coursePrice(BigDecimal.TEN)
                .currency(CurrencyType.BRL)
                .build();

        entityManager.persist(payment);

        entityManager.flush();
        entityManager.clear();

        boolean result = repository.existsByUserIdAndCourseId(1, 1);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserHasNotPaidForCourse() {
        Payment payment = Payment.builder()
                .userId(1)
                .courseId(1)
                .courseTitle("Java Course")
                .coursePrice(BigDecimal.TEN)
                .currency(CurrencyType.BRL)
                .build();

        entityManager.persist(payment);

        entityManager.flush();
        entityManager.clear();

        boolean result = repository.existsByUserIdAndCourseId(2, 10);

        assertThat(result).isFalse();
    }
}
