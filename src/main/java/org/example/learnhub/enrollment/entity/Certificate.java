package org.example.learnhub.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.learnhub.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(updatable = false, nullable = false)
    private String fullNameAtIssuance;

    @Column(updatable = false, nullable = false)
    private String courseNameAtIssuance;

    @Column(updatable = false, nullable = false)
    private Integer courseLengthInHoursAtIssuance;

    @CreationTimestamp
    private LocalDate issuedAt;
}
