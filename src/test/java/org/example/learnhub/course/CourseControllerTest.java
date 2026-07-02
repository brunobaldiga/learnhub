package org.example.learnhub.course;

import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.config.TokenService;
import org.example.learnhub.course.controller.CourseController;
import org.example.learnhub.course.dto.CourseResponse;
import org.example.learnhub.course.dto.CourseReviewResponse;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.repository.CourseRepository;
import org.example.learnhub.course.service.CourseReviewService;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@Import(SecurityConfiguration.class)
public class CourseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService service;

    @MockitoBean
    private CourseReviewService courseReviewService;

    @MockitoBean
    private CourseRepository repository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TokenService tokenService;

    @Test
    @WithMockUser(roles = "CREATOR")
    void shouldReturn201WhenCreatorCreatesCourse() throws Exception {
        when(service.create(any(), any())).thenReturn(new CourseResponse(
                1,
                1,
                "creator",
                "Java Course",
                CourseStatus.PRIVATE,
                BigDecimal.ZERO,
                0,
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/courses")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {"title": "Java Course"}
                        """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserTriesToCreateCourse() throws Exception {
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {"title": "Java"}
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CREATOR")
    void shouldReturn400WhenTitleIsMissing() throws Exception {
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].message").value("Title cannot be blank."));
    }

    @Test
    @WithMockUser(roles = "CREATOR")
    void shouldReturn200WhenCourseUpdatedSuccessfully() throws Exception {
        Integer courseId = 1;

        when(service.updateCourseById(any(), any(), any()))
                .thenReturn(new CourseResponse(
                        courseId,
                        1,
                        "creator",
                        "Updated Java Course",
                        CourseStatus.PRIVATE,
                        BigDecimal.ZERO,
                        0,
                        LocalDateTime.now()
                ));

        mockMvc.perform(patch("/api/courses/{courseId}", courseId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Updated Java Course",
                                    "status": "PRIVATE",
                                    "price": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(courseId))
                .andExpect(jsonPath("$.title").value("Updated Java Course"))
                .andExpect(jsonPath("$.status").value("PRIVATE"))
                .andExpect(jsonPath("$.price").value(0));
    }

    @Test
    @WithMockUser(roles = "CREATOR")
    void shouldReturn200WhenCourseSectionCreatedSuccessfully() throws Exception {
        Integer courseId = 1;

        when(service.createCourseSection(any(), any(), any()))
                .thenReturn(new SectionResponse(
                        1,
                        "Introduction",
                        0,
                        List.of()
                ));

        mockMvc.perform(post("/api/courses/{courseId}/sections", courseId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Introduction",
                                    "index": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Introduction"))
                .andExpect(jsonPath("$.index").value(0))
                .andExpect(jsonPath("$.videos").isArray());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserHasNoPermission() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "title": "Java Course"
                            }
                            """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn200WhenSearchingCourses() throws Exception {
        when(service.search(any(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/courses/search"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CREATOR")
    void shouldReturn200WhenListingCreatorCourses() throws Exception {
        when(service.findCourses(any(), any(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn200WhenFindingCourseById() throws Exception {
        when(service.findCourseById(any(), any()))
                .thenReturn(new CourseResponse(
                        1,
                        1,
                        "creator",
                        "Java Course",
                        CourseStatus.PUBLIC,
                        BigDecimal.ZERO,
                        0,
                        LocalDateTime.now()
                ));

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java Course"));
    }

    @Test
    @WithMockUser(roles = "CREATOR")
    void shouldReturn200WhenUpdatingSection() throws Exception {
        when(service.updateCourseSection(any(), any(), any()))
                .thenReturn(new SectionResponse(
                        1,
                        "Updated Section",
                        0,
                        List.of()
                ));

        mockMvc.perform(put("/api/courses/1/section/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "title":"Updated Section",
                                "index":0
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Section"));
    }

    @Test
    @WithMockUser(roles = "CREATOR")
    void shouldReturn204WhenDeletingSection() throws Exception {
        mockMvc.perform(delete("/api/courses/1/section/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(service)
                .deleteCourseSection(any(), eq(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn200WhenListingCourseSections() throws Exception {
        when(service.findCourseSection(any(), any()))
                .thenReturn(List.of(
                        new SectionResponse(
                                1,
                                "Introduction",
                                0,
                                List.of()
                        )
                ));

        mockMvc.perform(get("/api/courses/1/sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Introduction"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn201WhenCreatingCourseReview() throws Exception {
        when(courseReviewService.createCourseReview(any(), any(), any()))
                .thenReturn(new CourseReviewResponse(
                        "John",
                        5,
                        "Excellent course!",
                        LocalDateTime.now()
                ));

        mockMvc.perform(post("/api/courses/1/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "rating":5,
                                "comment":"Excellent course!"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorUsername").value("john"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn200WhenListingCourseReviews() throws Exception {
        when(courseReviewService.findCourseReviews(any(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/courses/1/reviews"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn204WhenDeletingCourseReview() throws Exception {
        mockMvc.perform(delete("/api/courses/1/reviews/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(courseReviewService)
                .deleteReviewById(any(), eq(1), eq(1));
    }
}
