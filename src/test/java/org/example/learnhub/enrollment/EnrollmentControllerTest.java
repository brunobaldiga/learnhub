package org.example.learnhub.enrollment;

import org.example.learnhub.config.JwtAuthenticationEntryPoint;
import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.config.TokenService;
import org.example.learnhub.enrollment.controller.EnrollmentController;
import org.example.learnhub.enrollment.dto.CertificateResponse;
import org.example.learnhub.enrollment.dto.EnrollmentResponse;
import org.example.learnhub.enrollment.dto.ProgressResponse;
import org.example.learnhub.enrollment.service.EnrollmentService;
import org.example.learnhub.exception.EntityNotFoundException;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnrollmentController.class)
@Import(SecurityConfiguration.class)
class EnrollmentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EnrollmentService service;
    @MockitoBean
    UserRepository userRepository;
    @MockitoBean
    TokenService tokenService;
    @MockitoBean
    JwtAuthenticationEntryPoint authenticationEntryPoint;

    private UsernamePasswordAuthenticationToken auth(RoleType role) {
        User user = User.builder().id(1).roleType(role).build();
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private EnrollmentResponse enrollmentResponse() {
        return new EnrollmentResponse(
                5, 10, "Java Course", "creator", 2, 4, 50.0, LocalDateTime.now()
        );
    }

    @ParameterizedTest
    @EnumSource(RoleType.class)
    void shouldFindEnrollmentById(RoleType role) throws Exception {
        when(service.findEnrollmentById(1, 5)).thenReturn(enrollmentResponse());

        mockMvc.perform(get("/api/enrollments/5")
                        .with(authentication(auth(role))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(10));
    }

    @Test
    void shouldMapMissingEnrollmentTo404() throws Exception {
        when(service.findEnrollmentById(1, 5)).thenThrow(new EntityNotFoundException("Enrollment not found."));

        mockMvc.perform(get("/api/enrollments/5")
                        .with(authentication(auth(RoleType.USER))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListOwnEnrollments() throws Exception {
        when(service.findEnrolledCourses(eq(1), any())).thenReturn(
                new PageImpl<>(List.of(enrollmentResponse()), PageRequest.of(0, 10), 1)
        );

        mockMvc.perform(get("/api/enrollments")
                        .param("page", "0")
                        .param("size", "10")
                        .with(authentication(auth(RoleType.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldStartLesson() throws Exception {
        when(service.startLesson(any(), eq(20))).thenReturn(new ProgressResponse(0, 4, 0.0, false));

        mockMvc.perform(post("/api/enrollments/lessons/20/start")
                        .with(authentication(auth(RoleType.USER))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalLessons").value(4));
    }

    @Test
    void shouldUpdateLessonProgress() throws Exception {
        when(service.progress(any(), eq(20), any())).thenReturn(new ProgressResponse(1, 4, 25.0, false));

        mockMvc.perform(patch("/api/enrollments/lessons/20/progress")
                        .with(authentication(auth(RoleType.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastPositionInSeconds":60}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedPercentage").value(25.0));
    }

    @Test
    void shouldValidateLessonProgress() throws Exception {
        mockMvc.perform(patch("/api/enrollments/lessons/20/progress")
                        .with(authentication(auth(RoleType.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastPositionInSeconds":-1}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGenerateCertificate() throws Exception {
        UUID id = UUID.randomUUID();
        CertificateResponse response = new CertificateResponse(id, "John Doe", "Java Course", 2, LocalDate.now());
        when(service.generateCertificate(any(), eq(5))).thenReturn(response);

        mockMvc.perform(post("/api/enrollments/5/certificates")
                        .with(authentication(auth(RoleType.USER))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString(id.toString())))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void shouldFindCertificateById() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findCertificateById(id)).thenReturn(
                new CertificateResponse(id, "John Doe", "Java Course", 2, LocalDate.now())
        );

        mockMvc.perform(get("/api/enrollments/certificates/{id}", id)
                        .with(authentication(auth(RoleType.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }
}
