package org.example.learnhub.enrollment.repository;

import org.example.learnhub.enrollment.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    boolean existsByEnrollmentId(Integer enrollmentId);
}
