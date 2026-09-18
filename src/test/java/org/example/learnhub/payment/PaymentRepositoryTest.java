package org.example.learnhub.payment;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
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
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PaymentRepositoryTest {
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
        user = entityManager.persist(User.builder()
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("password")
                .roleType(RoleType.USER)
                .build());

        Course course = entityManager.persist(Course.builder()
                .creatorId(user.getId())
                .title("Java Course")
                .status(CourseStatus.PUBLIC)
                .price(new BigDecimal("10.00"))
                .currency(CurrencyCode.USD)
                .build());

        entityManager.flush();
        courseId = course.getId();
    }

    @Test
    void shouldReturnPaymentByIdAndUserId() {
        Payment payment = entityManager.persist(newPayment(user.getId(), courseId));
        entityManager.flush();
        entityManager.clear();

        Optional<Payment> result = repository.findByIdAndUserId(payment.getId(), user.getId());

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getUserId()).isEqualTo(user.getId());
        assertThat(result.orElseThrow().getCourseId()).isEqualTo(courseId);
        assertThat(result.orElseThrow().getCourseCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(result.orElseThrow().getPaidCurrency()).isEqualTo(CurrencyCode.BRL);
    }

    @Test
    void shouldReturnEmptyWhenPaymentDoesNotBelongToUser() {
        User other = entityManager.persist(User.builder()
                .username("other")
                .email("other@example.com")
                .fullName("Other User")
                .password("password")
                .roleType(RoleType.USER)
                .build());

        Payment payment = entityManager.persist(newPayment(user.getId(), courseId));
        entityManager.flush();
        entityManager.clear();

        Optional<Payment> result = repository.findByIdAndUserId(payment.getId(), other.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenUserHasPaidForCourse() {
        entityManager.persist(newPayment(user.getId(), courseId));
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.existsByUserIdAndCourseId(user.getId(), courseId)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserHasNotPaidForCourse() {
        assertThat(repository.existsByUserIdAndCourseId(user.getId(), courseId)).isFalse();
    }

    @Test
    void shouldFilterPaymentsByUserAndDateRange() {
        Payment payment = entityManager.persist(newPayment(user.getId(), courseId));
        entityManager.flush();
        LocalDate paymentDate = payment.getCreatedAt().toLocalDate();
        entityManager.clear();

        Page<Payment> result = repository.findAll(
                filter(user.getId(), paymentDate, paymentDate),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(Payment::getId)
                .containsExactly(payment.getId());
    }

    @Test
    void shouldExcludePaymentsOutsideDateRange() {
        Payment payment = entityManager.persist(newPayment(user.getId(), courseId));
        entityManager.flush();
        LocalDate tomorrow = payment.getCreatedAt().toLocalDate().plusDays(1);
        entityManager.clear();

        Page<Payment> result = repository.findAll(
                filter(user.getId(), tomorrow, tomorrow),
                PageRequest.of(0, 10)
        );

        assertThat(result).isEmpty();
    }

    private Payment newPayment(Integer userId, Integer paymentCourseId) {
        return Payment.builder()
                .userId(userId)
                .courseId(paymentCourseId)
                .courseTitle("Java Course")
                .coursePrice(new BigDecimal("10.00"))
                .courseCurrency(CurrencyCode.USD)
                .exchangeRate(new BigDecimal("5.40"))
                .paidCurrency(CurrencyCode.BRL)
                .paidPrice(new BigDecimal("54.00"))
                .build();
    }
}
