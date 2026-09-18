package org.example.learnhub.user;

import org.example.learnhub.config.JwtAuthenticationEntryPoint;
import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.config.TokenService;
import org.example.learnhub.exception.EntityNotFoundException;
import org.example.learnhub.user.controller.UserController;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.dto.TokenResponse;
import org.example.learnhub.user.dto.UserResponse;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.repository.UserRepository;
import org.example.learnhub.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class)
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService service;
    @MockitoBean
    UserRepository repository;
    @MockitoBean
    TokenService tokenService;
    @MockitoBean
    JwtAuthenticationEntryPoint authenticationEntryPoint;

    private UsernamePasswordAuthenticationToken auth(RoleType role) {
        User user = User.builder().id(1).roleType(role).build();
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void shouldRegisterUser() throws Exception {
        when(service.register(any())).thenReturn(new TokenResponse("jwt-token"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"john",
                                  "email":"john@example.com",
                                  "fullName":"John Doe",
                                  "password":"123456"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void shouldValidateRegistration() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"a",
                                  "email":"not-an-email",
                                  "fullName":"1",
                                  "password":"12 34"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginUser() throws Exception {
        when(service.login(any())).thenReturn(new TokenResponse("jwt-token"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"john@example.com","password":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @ParameterizedTest
    @EnumSource(RoleType.class)
    void shouldReturnOwnProfile(RoleType role) throws Exception {
        when(service.findById(1)).thenReturn(new UserResponse(
                "john@example.com", "john", "John Doe", role, LocalDateTime.now()
        ));

        mockMvc.perform(get("/api/users/me")
                        .with(authentication(auth(role))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void shouldMapMissingProfileTo404() throws Exception {
        when(service.findById(1)).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/me")
                        .with(authentication(auth(RoleType.USER))))
                .andExpect(status().isNotFound());
    }
}
