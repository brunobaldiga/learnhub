package org.example.learnhub.section;

import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.config.TokenService;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.section.controller.SectionController;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.dto.LessonResponse;
import org.example.learnhub.section.repository.SectionRepository;
import org.example.learnhub.section.service.SectionService;
import org.example.learnhub.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SectionController.class)
@Import(SecurityConfiguration.class)
public class SectionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SectionService service;

    @MockitoBean
    private SectionRepository repository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TokenService tokenService;

    @Test
    @WithMockUser(roles = "CREATOR")
    void shouldReturn201WhenCreatorCreatesLesson() throws Exception {
        when(service.createLesson(any(), any(), any()))
                .thenReturn(
                        new SectionResponse(
                                1,
                                "Section 1",
                                0,
                                List.of(
                                        new LessonResponse(
                                                1,
                                                "https://youtube.com/lesson",
                                                0,
                                                LocalDateTime.now()
                                        )
                                )
                        )
                );

        mockMvc.perform(post("/api/sections/1/lessons")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "contentUrl":"https://youtube.com/video",
                            "index":0
                        }
                        """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserCreatesLesson() throws Exception {
        mockMvc.perform(post("/api/sections/1/lessons")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "contentUrl":"https://youtube.com/video",
                            "index":0
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CREATOR")
    void shouldReturn404WhenSectionDoesNotExistOnCreate() throws Exception {
        when(service.createLesson(any(), any(), any()))
                .thenThrow(new EntityNotFound("Section not found"));

        mockMvc.perform(post("/api/sections/1/lessons")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "contentUrl":"https://youtube.com/video",
                            "index":0
                        }
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CREATOR")
    void shouldReturn200WhenCreatorDeletesLesson() throws Exception {
        mockMvc.perform(delete("/api/sections/1/lessons/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserDeletesLesson() throws Exception {
        mockMvc.perform(delete("/api/sections/1/lessons/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}