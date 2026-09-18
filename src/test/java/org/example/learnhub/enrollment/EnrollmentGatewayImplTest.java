package org.example.learnhub.enrollment;

import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.enrollment.infra.EnrollmentGatewayImpl;
import org.example.learnhub.enrollment.service.EnrollmentService;
import org.example.learnhub.gateway.dto.EnrollmentInfo;
import org.example.learnhub.user.entity.User;
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
    private EnrollmentService service;

    @InjectMocks
    private EnrollmentGatewayImpl gateway;

    @Test
    void shouldDelegateEnrollment() {
        User user = User.builder().id(2).build();

        gateway.enroll(user, 10);

        verify(service).enroll(user, 10);
    }

    @Test
    void shouldDelegateEnrollmentExistenceCheck() {
        when(service.existsByUserIdAndCourseId(2, 10)).thenReturn(true);

        assertThat(gateway.existsByUserIdAndCourseId(2, 10)).isTrue();
    }

    @Test
    void shouldMapEnrollmentEntityToInfo() {
        LocalDateTime enrolledAt = LocalDateTime.of(2026, 9, 15, 10, 30);
        Enrollment enrollment = Enrollment.builder()
                .id(5)
                .userId(2)
                .courseId(10)
                .enrolledAt(enrolledAt)
                .build();
        when(service.findEnrollmentEntityByUserIdAndCourseId(2, 10)).thenReturn(Optional.of(enrollment));

        Optional<EnrollmentInfo> result = gateway.findByUserIdAndCourseId(2, 10);

        assertThat(result).contains(new EnrollmentInfo(5, 2, 10, enrolledAt));
    }

    @Test
    void shouldReturnEmptyWhenEnrollmentDoesNotExist() {
        when(service.findEnrollmentEntityByUserIdAndCourseId(2, 10)).thenReturn(Optional.empty());

        assertThat(gateway.findByUserIdAndCourseId(2, 10)).isEmpty();
    }
}
