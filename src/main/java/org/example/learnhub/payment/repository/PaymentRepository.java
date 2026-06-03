package org.example.learnhub.payment.repository;

import org.example.learnhub.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    @Query("SELECT payment FROM Payment payment WHERE payment.userId = :userId " +
            "AND (:startDate IS NULL OR payment.createdAt >= :startDate)" +
            "AND (:startDate IS NULL OR payment.createdAt <= :endDate)")
    Page<Payment> findByUserIdAndDateRange(Integer id, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Optional<Payment> findByIdAndUserId(Integer id, Integer paymentId);

    boolean existsByUserIdAndCourseId(Integer id, Integer courseId);
}
