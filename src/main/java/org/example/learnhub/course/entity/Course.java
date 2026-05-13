package org.example.learnhub.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.learnhub.sections.entity.Section;
import org.example.learnhub.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Builder.Default
    private CourseStatus status = CourseStatus.PRIVATE;

    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Builder.Default
    private Integer salesAmount = 0;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Section> sections = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
