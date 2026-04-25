package org.example.learnhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.learnhub.types.CourseStatus;

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

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User creator;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CourseStatus status;
}
