package org.example.learnhub.section;

import org.example.learnhub.gateway.dto.LessonInfo;
import org.example.learnhub.section.entity.Lesson;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.infra.LessonGatewayImpl;
import org.example.learnhub.section.service.SectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonGatewayImplTest {
    @Mock
    private SectionService service;

    @InjectMocks
    private LessonGatewayImpl gateway;

    @Test
    void shouldDelegateCourseDurationCalculation() {
        when(service.calculateDurationByCourseId(10)).thenReturn(360);

        assertThat(gateway.calculateDurationByCourseId(10)).isEqualTo(360);
        verify(service).calculateDurationByCourseId(10);
    }

    @Test
    void shouldMapLessonEntityToLessonInfo() {
        Section section = Section.builder().id(4).courseId(10).build();
        Lesson lesson = Lesson.builder().id(8).duration(120).section(section).build();
        when(service.findLessonEntityById(8)).thenReturn(lesson);

        LessonInfo result = gateway.findById(8);

        assertThat(result).isEqualTo(new LessonInfo(8, 120, 4, 10));
    }
}
