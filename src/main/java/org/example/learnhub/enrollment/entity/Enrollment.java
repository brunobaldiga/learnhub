package org.example.learnhub.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "enrollments",
    uniqueConstraints =
    @UniqueConstraint(columnNames = {"user_id", "course_id"})
)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    @Builder.Default
    private Integer completedLessons = 0;

    @Column(nullable = false)
    private Integer totalLessons;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime enrolledAt;
}
