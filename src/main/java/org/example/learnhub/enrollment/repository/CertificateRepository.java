package org.example.learnhub.enrollment.repository;

import org.example.learnhub.enrollment.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface CertificateRepository extends JpaRepository<Certificate, UUID> {
}
