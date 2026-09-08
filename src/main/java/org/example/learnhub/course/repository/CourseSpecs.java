package org.example.learnhub.course.repository;

import org.example.learnhub.course.dto.CourseFilter;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class CourseSpecs {
    public static Specification<Course> withFilter(CourseFilter filter, List<Integer> creatorIds) {
        return Specification
                .where(titleContains(filter.title()))
                .and(hasCreator(creatorIds));
    }

    private static Specification<Course> titleContains(String title) {
        return (root, query, criteriaBuilder) -> {
            if(title == null || title.isBlank()) return null;

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"
            );
        };
    }

    private static Specification<Course> hasCreator(List<Integer> creatorIds) {
        return (root, query, criteriaBuilder) -> {
            if(creatorIds == null) return null;
            if(creatorIds.isEmpty()) return criteriaBuilder.disjunction();

            return root.get("creatorId").in(creatorIds);
        };
    }

    public static Specification<Course> ownedBy(Integer userId) {
        return (root, query, criteriaBuilder) -> {
            if(userId == null) return null;

            return criteriaBuilder.equal(root.get("creatorId"), userId);
        };
    }

    public static Specification<Course> isPublic() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), CourseStatus.PUBLIC);
    }
}
