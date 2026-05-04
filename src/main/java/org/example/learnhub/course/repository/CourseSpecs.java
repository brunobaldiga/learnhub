package org.example.learnhub.course.repository;

import org.example.learnhub.course.dto.CourseFilter;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.springframework.data.jpa.domain.Specification;

public class CourseSpecs {
    public static Specification<Course> withFilter(CourseFilter filter) {
        return Specification
                .where(titleContains(filter.title()))
                .and(hasCreator(filter.creatorName()));
    }

    private static Specification<Course> titleContains(String title) {
        return (root, query, criteriaBuilder) -> {
            if (title == null || title.isBlank()) return null;

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"
            );
        };
    }

    private static Specification<Course> hasCreator(String creatorUsername) {
        return (root, query, criteriaBuilder) -> {
            if (creatorUsername == null || creatorUsername.isBlank()) return null;

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("creator").get("username")),
                    "%" + creatorUsername.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Course> ownedBy(Integer userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) return null;

            return criteriaBuilder.equal(root.get("creator").get("id"), userId);
        };
    }

    public static Specification<Course> isPublic() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), CourseStatus.PUBLIC);
    }
}
