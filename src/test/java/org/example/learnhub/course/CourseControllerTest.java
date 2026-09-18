package org.example.learnhub.course;

import org.example.learnhub.config.JwtAuthenticationEntryPoint;
import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.config.TokenService;
import org.example.learnhub.course.controller.CourseController;
import org.example.learnhub.course.dto.CourseResponse;
import org.example.learnhub.course.dto.CourseReviewResponse;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.service.CourseReviewService;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    void shouldSearchCourses() throws Exception {
        when(service.search(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/courses/search")
                        .with(authentication(auth(student))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldListCreatorCourses() throws Exception {
        when(service.findUserCourses(any(), any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/courses")
                        .with(authentication(auth(creator))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFindCourseById() throws Exception {
        when(service.findById(any(), eq(10))).thenReturn(courseResponse());

        mockMvc.perform(get("/api/courses/10")
                        .with(authentication(auth(student))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Course"));
    }

    @Test
    void shouldUpdateCourse() throws Exception {
        CourseResponse updated = new CourseResponse(
                10, 1, "creator", "Updated Course", CourseStatus.PUBLIC,
                BigDecimal.valueOf(149.99), CurrencyCode.BRL, 3, LocalDateTime.now()
        );

        when(service.updateById(any(), eq(10), any())).thenReturn(updated);

        mockMvc.perform(patch("/api/courses/10")
                        .with(authentication(auth(creator)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title":"Updated Course",
                                "status":"PUBLIC",
                                "price":149.99,
                                "currency":"BRL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Course"))
                .andExpect(jsonPath("$.currency").value("BRL"));
    }

    @Test
    void shouldCreateCourseSection() throws Exception {
        when(service.createCourseSection(any(), eq(10), any()))
                .thenReturn(new SectionResponse(20, "Introduction", 1, List.of()));

        mockMvc.perform(post("/api/courses/10/sections")
                        .with(authentication(auth(creator)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title":"Introduction",
                                "position":1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20));
    }

    @Test
    void shouldUpdateCourseSection() throws Exception {
        when(service.updateCourseSection(any(), eq(20), any()))
                .thenReturn(new SectionResponse(20, "Updated", 2, List.of()));

        mockMvc.perform(put("/api/courses/sections/20")
                        .with(authentication(auth(creator)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title":"Updated",
                                "position":2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void shouldDeleteCourseSection() throws Exception {
        mockMvc.perform(delete("/api/courses/sections/20")
                        .with(authentication(auth(creator))))
                .andExpect(status().isNoContent());

        verify(service).deleteCourseSection(creator, 20);
    }

    @Test
    void shouldListCourseSections() throws Exception {
        when(service.findCourseSection(any(), eq(10)))
                .thenReturn(List.of(new SectionResponse(20, "Introduction", 1, List.of())));

        mockMvc.perform(get("/api/courses/10/sections")
                        .with(authentication(auth(student))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Introduction"));
    }

    @Test
    void shouldCreateCourseReview() throws Exception {
        when(courseReviewService.create(any(), eq(10), any()))
                .thenReturn(new CourseReviewResponse(30, "student", 5, "Excellent course!", LocalDateTime.now()));

        mockMvc.perform(post("/api/courses/10/reviews")
                        .with(authentication(auth(student)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rating":5,"comment":"Excellent course!"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.authorUsername").value("student"));
    }

    @Test
    void shouldRejectInvalidCourseReview() throws Exception {
        mockMvc.perform(post("/api/courses/10/reviews")
                        .with(authentication(auth(student)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rating":6,"comment":"bad"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListCourseReviews() throws Exception {
        when(courseReviewService.findCourseReviews(eq(10), any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/courses/10/reviews")
                        .with(authentication(auth(student))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteOwnCourseReview() throws Exception {
        mockMvc.perform(delete("/api/courses/10/reviews/30")
                        .with(authentication(auth(student))))
                .andExpect(status().isNoContent());

        verify(courseReviewService).deleteById(student, 10, 30);
    }

}
