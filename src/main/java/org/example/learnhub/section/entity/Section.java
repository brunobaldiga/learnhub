package org.example.learnhub.section.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.learnhub.exception.MaxLessonsReachedException;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private static final Integer MAX_LESSONS = 20;

    public void addLesson(Lesson lesson) {
        if(lessons.size() >= MAX_LESSONS) {
            throw new MaxLessonsReachedException("A section cannot contain more than " + MAX_LESSONS + " lessons.");
        }

        lesson.setSection(this);
        lessons.add(lesson);

    }
}
