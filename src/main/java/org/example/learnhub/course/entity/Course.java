package org.example.learnhub.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "creator_id", nullable = false)
    private Integer creatorId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CourseStatus status = CourseStatus.PRIVATE;

    @Builder.Default
    private Integer salesAmount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @Builder.Default
    @Column(nullable = false, precision = 38, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyCode currency = CurrencyCode.USD;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void addReview(Integer rating) {
        averageRating = (averageRating * totalReviews + rating) / (totalReviews + 1);
        totalReviews++;
    }

    public void removeReview(Integer rating) {
        if(totalReviews <= 0) {
            throw new IllegalStateException("Course has no reviews");
        }

        if(totalReviews > 1) {
            totalReviews = 0;
            averageRating = 0.0;

            return;
        }
        averageRating = (averageRating * totalReviews - rating) / (totalReviews - 1);
        totalReviews--;
    }
}
