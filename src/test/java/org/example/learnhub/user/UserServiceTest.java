package org.example.learnhub.user;

import org.example.learnhub.config.TokenService;
import org.example.learnhub.exception.EmailAlreadyInUse;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.exception.UsernameAlreadyInUse;
import org.example.learnhub.user.dto.TokenResponse;
import org.example.learnhub.user.dto.UserLoginRequest;
import org.example.learnhub.user.dto.UserRegisterRequest;
import org.example.learnhub.user.dto.UserResponse;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.repository.UserRepository;
import org.example.learnhub.user.service.UserMapper;
import org.example.learnhub.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository repository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .username("john")
                .email("john@email.com")
                .password("encoded-password")
                .build();
    }

    @Test
    void shouldRegisterSuccessfully() {
        UserRegisterRequest request = new UserRegisterRequest(
                "john",
                "john@email.com",
                "123456"
        );

        when(repository.existsByEmail(request.email())).thenReturn(false);
        when(repository.existsByUsername(request.username())).thenReturn(false);

        when(mapper.toUser(request)).thenReturn(user);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(tokenService.generateToken(user))
                .thenReturn("jwt-token");

        TokenResponse result = service.register(request);

        verify(repository).save(user);

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() {
        UserRegisterRequest request = new UserRegisterRequest(
                "john",
                "john@email.com",
                "123456"
        );

        when(repository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(EmailAlreadyInUse.class)
                .hasMessage("Email is already in use.");
    }

    @Test
    void shouldReturn409WhenUsernameAlreadyExists() {
        UserRegisterRequest request = new UserRegisterRequest(
                "john",
                "john@email.com",
                "123456"
        );

        when(repository.existsByEmail(request.email())).thenReturn(false);
        when(repository.existsByUsername(request.username())).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(UsernameAlreadyInUse.class)
                .hasMessage("Username is already in use.");
    }

    @Test
    void shouldLoginSuccessfully() {
        UserLoginRequest request = new UserLoginRequest(
                "john@email.com",
                "123456"
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(tokenService.generateToken(user))
                .thenReturn("jwt-token");

        TokenResponse result = service.login(request);

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void shouldReturnUserWhenUserExists() {
        UserResponse response = new UserResponse(
                user.getEmail(),
                user.getUsername(),
                user.getRoleType(),
                LocalDateTime.now()
        );

        when(repository.findById(1))
                .thenReturn(Optional.of(user));

        when(mapper.toDto(user))
                .thenReturn(response);

        UserResponse result = service.findById(1);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() {
        when(repository.findById(any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("User not found");
    }
}