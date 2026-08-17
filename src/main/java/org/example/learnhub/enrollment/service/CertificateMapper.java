package org.example.learnhub.enrollment.service;

import org.example.learnhub.enrollment.dto.CertificateResponse;
import org.example.learnhub.enrollment.entity.Certificate;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CertificateMapper {

    public Certificate toCertificate(User user, Enrollment enrollment) {
        return Certificate.builder()
                .enrollment(enrollment)
                .fullNameAtIssuance(user.getFullName())
                .courseTitleAtIssuance(enrollment.getCourse().getTitle())
                .courseLengthInHoursAtIssuance(enrollment.getCourse().calculateDuration() / 3600)
                .issuedAt(LocalDate.now())
                .build();
    }

    public CertificateResponse toDto(Certificate certificate) {
        return new CertificateResponse(
                certificate.getId(),
                certificate.getFullNameAtIssuance(),
                certificate.getCourseTitleAtIssuance(),
                certificate.getCourseLengthInHoursAtIssuance(),
                certificate.getIssuedAt()
        );
    }
}
