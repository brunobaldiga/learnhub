package org.example.learnhub.course.entity;

import jakarta.persistence.*;
import lombok.*;
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
    private BigDecimal price = BigDecimal.ZERO;

    @Builder.Default
    private Integer salesAmount = 0;

    @Column(nullable = false)
    private Double averageRating;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void addReview(Integer rating) {
        averageRating = (averageRating * totalReviews + rating) / (totalReviews + 1);
        totalReviews++;
    }

    public void removeReview(Integer rating) {
        averageRating = (averageRating * totalReviews - rating) / (totalReviews - 1);
        totalReviews--;
    }
}
