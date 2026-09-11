package org.example.learnhub.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "certificates")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(updatable = false, nullable = false)
    private String fullNameAtIssuance;

    @Column(updatable = false, nullable = false)
    private String courseTitleAtIssuance;

    @Column(updatable = false, nullable = false)
    private Integer courseLengthInHoursAtIssuance;

    @CreationTimestamp
    private LocalDate issuedAt;
}
