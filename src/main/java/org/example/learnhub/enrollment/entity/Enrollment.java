package org.example.learnhub.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.user.entity.User;
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private Integer totalLessons;

    @OneToMany(mappedBy = "enrollment", fetch = FetchType.LAZY)
    private List<LessonProgress> lessonProgresses = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime enrolledAt;
}
