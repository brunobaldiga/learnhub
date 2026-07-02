package org.example.learnhub.user;

import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.config.TokenService;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.user.controller.UserController;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.dto.TokenResponse;
import org.example.learnhub.user.dto.UserResponse;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.repository.UserRepository;
import org.example.learnhub.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private UserRepository repository;

    @MockitoBean
    private TokenService tokenService;

    @Test
    void shouldReturn201WhenUserRegisters() throws Exception {
        when(service.register(any()))
                .thenReturn(new TokenResponse("jwt-token"));

        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "username":"john",
                            "email":"john@email.com",
                            "password":"123456"
                        }
                        """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn200WhenUserLogsIn() throws Exception {
        when(service.login(any()))
                .thenReturn(new TokenResponse("jwt-token"));

        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "identifier":"john@email.com",
                            "password":"123456"
                        }
                        """))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "CREATOR", "ADMIN"})
    void shouldReturn200WhenUserRequestsOwnProfile(String role) throws Exception {
        User user = User.builder().id(1).build();

        when(service.findById(any()))
                .thenReturn(
                        new UserResponse(
                                "john@email.com",
                                "john",
                                RoleType.USER,
                                LocalDateTime.now()
                        )
                );

        mockMvc.perform(get("/api/users/me")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        )))
                ).andExpect(status().isOk());
    }

    @Test
    void shouldReturn403WhenAnonymousUserRequestsProfile() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
        User user = User.builder()
                .id(1)
                .build();

        when(service.findById(any()))
                .thenThrow(new EntityNotFound("User not found"));

        mockMvc.perform(get("/api/users/me")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                )
                        )))
                .andExpect(status().isNotFound());
    }
}