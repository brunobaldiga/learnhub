package org.example.learnhub.course;

import org.example.learnhub.config.JwtAuthenticationEntryPoint;
import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.config.TokenService;
import org.example.learnhub.course.controller.CourseController;
import org.example.learnhub.course.dto.CourseResponse;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.service.CourseReviewService;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
@Import(SecurityConfiguration.class)
public class CourseControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CourseService service;
    @MockitoBean
    CourseReviewService courseReviewService;
    @MockitoBean
    UserRepository userRepository;
    @MockitoBean
    TokenService tokenService;
    @MockitoBean
    JwtAuthenticationEntryPoint authenticationEntryPoint;

    private User creator;
    private User student;

    @BeforeEach
    void setUp() {
        creator = User.builder().id(1).username("creator").roleType(RoleType.CREATOR).build();
        student = User.builder().id(2).username("student").roleType(RoleType.USER).build();
    }

    private UsernamePasswordAuthenticationToken auth(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private CourseResponse courseResponse() {
        return new CourseResponse(
                10, 1, "creator", "Java Course", CourseStatus.PUBLIC,
                BigDecimal.valueOf(99.9), CurrencyCode.USD, 3, LocalDateTime.now()
        );
    }

    @Test
    void shouldCreateCourseAsCreator() throws Exception {
        when(service.create(any(), any())).thenReturn(courseResponse());

        mockMvc.perform(post("/api/courses")
                        .with(authentication(auth(creator)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Java Course"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void shouldRejectCourseCreationByRegularUser() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .with(authentication(auth(student)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Java Course"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldValidateCourseTitle() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .with(authentication(auth(creator)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }
}
