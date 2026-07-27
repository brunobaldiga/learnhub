package org.example.learnhub.payment.repository;

import org.example.learnhub.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer>, JpaSpecificationExecutor<Payment> {
    Optional<Payment> findByIdAndUserId(Integer id, Integer userId);
    boolean existsByUserIdAndCourseId(Integer id, Integer courseId);
}
