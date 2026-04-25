package org.example.learnhub.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "course_progress",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"user_id", "course_id"}) // the combination of user_id and course_id cannot repeat
)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseProgress {
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
    private Double progress;
}
