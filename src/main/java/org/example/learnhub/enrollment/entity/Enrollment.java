package org.example.learnhub.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @OneToMany(mappedBy = "enrollment", fetch = FetchType.LAZY)
    @Builder.Default
    private List<LessonProgress> lessonProgresses = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime enrolledAt;
}
