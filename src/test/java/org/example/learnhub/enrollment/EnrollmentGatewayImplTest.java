package org.example.learnhub.enrollment;

import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.enrollment.infra.EnrollmentGatewayImpl;
import org.example.learnhub.enrollment.repository.EnrollmentRepository;
import org.example.learnhub.gateway.dto.EnrollmentInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentGatewayImplTest {
    @Mock
    private EnrollmentRepository repository;

    @InjectMocks
    private EnrollmentGatewayImpl gateway;

    @Test
    void shouldReturnTrueWhenEnrollmentExists() {
        when(repository.existsByUserIdAndCourseId(2, 10))
                .thenReturn(true);

        boolean result =
                gateway.existsByUserIdAndCourseId(2, 10);

        assertThat(result).isTrue();

        verify(repository)
                .existsByUserIdAndCourseId(2, 10);
    }

    @Test
    void shouldMapEnrollmentEntityToInfo() {
        LocalDateTime enrolledAt =
                LocalDateTime.of(2026, 9, 15, 10, 30);

        Enrollment enrollment = Enrollment.builder()
                .id(5)
                .userId(2)
                .courseId(10)
                .enrolledAt(enrolledAt)
                .build();

        when(repository.findByUserIdAndCourseId(2, 10))
                .thenReturn(Optional.of(enrollment));

        Optional<EnrollmentInfo> result =
                gateway.findByUserIdAndCourseId(2, 10);

        assertThat(result)
                .contains(
                        new EnrollmentInfo(
                                5,
                                2,
                                10,
                                enrolledAt
                        )
                );

        verify(repository)
                .findByUserIdAndCourseId(2, 10);
    }

    @Test
    void shouldReturnEmptyWhenEnrollmentDoesNotExist() {
        when(repository.findByUserIdAndCourseId(2, 10))
                .thenReturn(Optional.empty());

        Optional<EnrollmentInfo> result =
                gateway.findByUserIdAndCourseId(2, 10);

        assertThat(result).isEmpty();

        verify(repository)
                .findByUserIdAndCourseId(2, 10);
    }
}
