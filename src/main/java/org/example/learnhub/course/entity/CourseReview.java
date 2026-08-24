package org.example.learnhub.course.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_reviews")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "author_id", nullable = false)
    private Integer author;

    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer rating;

    @Column(nullable = false, length = 500)
    private String comment;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
