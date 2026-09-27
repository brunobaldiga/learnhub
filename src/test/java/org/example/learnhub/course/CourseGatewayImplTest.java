package org.example.learnhub.course;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.infra.CourseGatewayImpl;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.gateway.dto.CourseInfo;
import org.example.learnhub.gateway.dto.CourseSummary;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseGatewayImplTest {
    @Mock
    private CourseService service;

    @InjectMocks
    private CourseGatewayImpl gateway;

    @Test
    void shouldMapCourseToCourseInfoUsingCourseId() {
        Integer userId = 7;
        Integer courseId = 42;
        Course course = Course.builder()
                .id(courseId)
                .creatorId(3)
                .title("Java")
                .price(new BigDecimal("29.90"))
                .currency(CurrencyCode.USD)
                .status(CourseStatus.PUBLIC)
                .build();
        when(service.findEntityById(courseId)).thenReturn(course);

        CourseInfo result = gateway.findById(courseId);

        assertThat(result).isEqualTo(new CourseInfo(
                42,
                3,
                "Java",
                new BigDecimal("29.90"),
                CurrencyCode.USD,
                CourseStatus.PUBLIC
        ));
        verify(service).findEntityById(courseId);
    }

    @Test
    void shouldDelegateCreatorCheck() {
        when(service.isCourseCreator(10, 2)).thenReturn(true);

        assertThat(gateway.isCourseCreator(10, 2)).isTrue();
        verify(service).isCourseCreator(10, 2);
    }

    @Test
    void shouldDelegateCourseSummariesLookup() {
        Map<Integer, CourseSummary> summaries = Map.of(10, new CourseSummary(10, "Java", "creator"));
        when(service.findCourseSummariesById(List.of(10))).thenReturn(summaries);

        assertThat(gateway.findCourseSummariesById(List.of(10))).isSameAs(summaries);
    }

    @Test
    void shouldDelegateLessonCountAndSummaryLookup() {
        CourseSummary summary = new CourseSummary(10, "Java", "creator");
        when(service.countLessonsByCourseId(10)).thenReturn(12);
        when(service.findCourseSummaryById(10)).thenReturn(summary);

        assertThat(gateway.countLessonsByCourseId(10)).isEqualTo(12);
        assertThat(gateway.findCourseSummaryById(10)).isEqualTo(summary);
    }

    @Test
    void shouldDelegateSalesAndReviewUpdates() {
        gateway.incrementSalesAmount(10);
        gateway.recordReview(10, 5);

        verify(service).incrementSalesAmount(10);
        verify(service).recordReview(10, 5);
    }
}
