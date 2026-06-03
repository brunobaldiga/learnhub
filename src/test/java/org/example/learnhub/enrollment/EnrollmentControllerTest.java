package org.example.learnhub.enrollment;

import org.example.learnhub.config.TokenService;
import org.example.learnhub.enrollment.controller.EnrollmentController;
import org.example.learnhub.enrollment.dto.EnrollmentResponse;
import org.example.learnhub.enrollment.repository.EnrollmentRepository;
import org.example.learnhub.enrollment.service.EnrollmentService;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
public class EnrollmentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnrollmentService service;

    @MockitoBean
    private EnrollmentRepository repository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TokenService tokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "CREATOR", "ADMIN"})
    void shouldReturn200WhenEnrollmentExists(String role) throws Exception {
        Integer enrollmentId = 1;

        when(service.findEnrollmentById(any(), any())).thenReturn(new EnrollmentResponse(
                enrollmentId, 1, "Java Course", "Creator",
                0, 10, 0.0, LocalDateTime.now()
        ));

        mockMvc.perform(get("/api/enrollments/{enrollmentId}", enrollmentId)
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)))
                ))
        )

        .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "CREATOR", "ADMIN"})
    void shouldReturn404WhenEnrollmentDoesNotExists(String role) throws Exception {
        Integer enrollmentId = 1;

        when(service.findEnrollmentById(any(), any())).thenThrow(
                new EntityNotFound("Enrollment not found")
        );

        mockMvc.perform(get("/api/enrollments/{enrollmentId}", enrollmentId)
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)))
                ))
        ).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "CREATOR", "ADMIN"})
    void shouldReturn200WhenUserFindsOwnEnrollments(String role) throws Exception {
        EnrollmentResponse enrollmentResponse = new EnrollmentResponse(
                1, 1, "Java Course", "Creator",
                0, 10, 0.0, LocalDateTime.now()
        );

        List<EnrollmentResponse> list = List.of(enrollmentResponse);
        Page<EnrollmentResponse> page = new PageImpl<>(list, PageRequest.of(0, 10), list.size());
        when(service.findEnrolledCourses(any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/enrollments")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)))
                ))
            ).andExpect(status().isOk())
             .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }
}
