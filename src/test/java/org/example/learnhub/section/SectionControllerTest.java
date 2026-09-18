package org.example.learnhub.section;

import org.example.learnhub.config.JwtAuthenticationEntryPoint;
import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.config.TokenService;
import org.example.learnhub.exception.EntityNotFoundException;
import org.example.learnhub.section.controller.SectionController;
import org.example.learnhub.section.dto.LessonResponse;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.service.SectionService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SectionController.class)
@Import(SecurityConfiguration.class)
class SectionControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SectionService service;
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
        creator = User.builder().id(1).roleType(RoleType.CREATOR).build();
        student = User.builder().id(2).roleType(RoleType.USER).build();
    }

    private UsernamePasswordAuthenticationToken auth(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void shouldCreateLessonAsCreator() throws Exception {
        when(service.createLesson(any(), eq(10), any())).thenReturn(new SectionResponse(
                10, "Introduction", 1,
                List.of(new LessonResponse(20, "https://example.com/lesson", 120, 1, null))
        ));

        mockMvc.perform(post("/api/sections/10/lessons")
                        .with(authentication(auth(creator)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentUrl":"https://example.com/lesson","duration":120,"position":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lessons[0].duration").value(120));
    }

    @Test
    void shouldRejectInvalidLessonRequest() throws Exception {
        mockMvc.perform(post("/api/sections/10/lessons")
                        .with(authentication(auth(creator)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentUrl":"x","duration":0,"position":0}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectLessonCreationByRegularUser() throws Exception {
        mockMvc.perform(post("/api/sections/10/lessons")
                        .with(authentication(auth(student)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentUrl":"https://example.com/lesson","duration":120,"position":1}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldMapMissingSectionTo404() throws Exception {
        when(service.createLesson(any(), eq(10), any()))
                .thenThrow(new EntityNotFoundException("Section not found"));

        mockMvc.perform(post("/api/sections/10/lessons")
                        .with(authentication(auth(creator)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentUrl":"https://example.com/lesson","duration":120,"position":1}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteLessonAsCreator() throws Exception {
        mockMvc.perform(delete("/api/sections/10/lessons/20")
                        .with(authentication(auth(creator))))
                .andExpect(status().isNoContent());

        verify(service).deleteLesson(creator, 10, 20);
    }

    @Test
    void shouldListSectionLessons() throws Exception {
        when(service.findSectionLessons(student, 10)).thenReturn(List.of(
                new LessonResponse(20, "https://example.com/lesson", 120, 1, null)
        ));

        mockMvc.perform(get("/api/sections/10/lessons")
                        .with(authentication(auth(student))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20));
    }

    @Test
    void shouldFindLessonById() throws Exception {
        when(service.findLessonById(student, 20)).thenReturn(
                new LessonResponse(20, "https://example.com/lesson", 120, 1, null)
        );

        mockMvc.perform(get("/api/sections/lessons/20")
                        .with(authentication(auth(student))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20));
    }
}
