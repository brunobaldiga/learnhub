package org.example.learnhub.section;

import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.config.TokenService;
import org.example.learnhub.exception.EntityNotFoundException;
import org.example.learnhub.section.controller.SectionController;
import org.example.learnhub.section.dto.LessonResponse;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.repository.SectionRepository;
import org.example.learnhub.section.service.SectionService;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    private User creator;
    private User user;
    private UsernamePasswordAuthenticationToken creatorAuth;
    private UsernamePasswordAuthenticationToken userAuth;


    @BeforeEach
    void setUp() {
        creator = User.builder().id(1).username("creator").build();
        user = User.builder().id(2).username("user").build();
        creatorAuth = new UsernamePasswordAuthenticationToken(creator, null, List.of(new SimpleGrantedAuthority("ROLE_CREATOR"), new SimpleGrantedAuthority("ROLE_USER")));
        userAuth = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldReturn201WhenCreatorCreatesLesson() throws Exception {
        when(service.createLesson(any(), eq(10), any())).thenReturn(new SectionResponse(10, "Introduction", 1, List.of(new LessonResponse(20, "https://example.com/lesson", 1, null))));

        mockMvc.perform(post("/api/sections/10/lessons").with(authentication(creatorAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentUrl\":\"https://example.com/lesson\",\"duration\":120,\"position\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void shouldReturn400WhenLessonRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/sections/10/lessons").with(authentication(creatorAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentUrl\":\"x\",\"duration\":0,\"position\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn403WhenUserCreatesLesson() throws Exception {
        mockMvc.perform(post("/api/sections/10/lessons").with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentUrl\":\"https://example.com/lesson\",\"duration\":120,\"position\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn404WhenSectionDoesNotExist() throws Exception {
        when(service.createLesson(any(), eq(10), any())).thenThrow(new EntityNotFoundException("Section not found"));

        mockMvc.perform(post("/api/sections/10/lessons").with(authentication(creatorAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentUrl\":\"https://example.com/lesson\",\"duration\":120,\"position\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn204WhenDeletingLesson() throws Exception {
        mockMvc.perform(delete("/api/sections/10/lessons/20").with(authentication(creatorAuth)))
                .andExpect(status().isNoContent());
        verify(service).deleteLesson(creator, 10, 20);
    }

    @Test
    void shouldReturn403WhenUserDeletesLesson() throws Exception {
        mockMvc.perform(delete("/api/sections/10/lessons/20").with(authentication(userAuth)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn200WhenListingSectionLessons() throws Exception {
        when(service.findSectionLessons(user, 10)).thenReturn(List.of(new LessonResponse(20, "https://example.com/lesson", 1, null)));

        mockMvc.perform(get("/api/sections/10/lessons").with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20));
    }

    @Test
    void shouldReturn200WhenFindingLesson() throws Exception {
        when(service.findLessonById(user, 20)).thenReturn(new LessonResponse(20, "https://example.com/lesson", 1, null));

        mockMvc.perform(get("/api/sections/lessons/20").with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20));
    }

    @Test
    void shouldReturn403WhenAnonymousUserListsLessons() throws Exception {
        mockMvc.perform(get("/api/sections/10/lessons")).andExpect(status().isForbidden());
    }
}