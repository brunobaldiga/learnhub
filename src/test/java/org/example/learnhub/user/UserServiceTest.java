package org.example.learnhub.user;

import org.example.learnhub.config.TokenService;
import org.example.learnhub.exception.EmailAlreadyInUseException;
import org.example.learnhub.exception.EntityNotFoundException;
import org.example.learnhub.exception.UsernameAlreadyInUseException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserMapper mapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserRepository repository;
    @Mock AuthenticationManager authenticationManager;
    @Mock TokenService tokenService;

    @InjectMocks UserService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .username("john")
                .email("john@example.com")
                .fullName("John Doe")
                .password("encoded")
                .build();
    }

    @Test
    void shouldRegisterAndReturnToken() {
        UserRegisterRequest request = new UserRegisterRequest("john", "john@example.com", "John Doe", "123456");
        when(repository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(repository.existsByUsernameIgnoreCase(request.username())).thenReturn(false);
        when(mapper.toUser(request)).thenReturn(user);
        when(passwordEncoder.encode("123456")).thenReturn("encoded");

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenService.generateToken(user)).thenReturn("jwt-token");

        assertThat(service.register(request)).isEqualTo(new TokenResponse("jwt-token"));
        assertThat(user.getPassword()).isEqualTo("encoded");
        verify(repository).save(user);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        UserRegisterRequest request = new UserRegisterRequest("john", "john@example.com", "John Doe", "123456");
        when(repository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(EmailAlreadyInUseException.class)
                .hasMessage("Email is already in use.");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateUsername() {
        UserRegisterRequest request = new UserRegisterRequest("john", "john@example.com", "John Doe", "123456");
        when(repository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(repository.existsByUsernameIgnoreCase(request.username())).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(UsernameAlreadyInUseException.class)
                .hasMessage("Username is already in use.");
    }

    @Test
    void shouldLoginAndReturnToken() {
        UserLoginRequest request = new UserLoginRequest("john@example.com", "123456");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenService.generateToken(user)).thenReturn("jwt-token");

        assertThat(service.login(request)).isEqualTo(new TokenResponse("jwt-token"));
    }

    @Test
    void shouldFindUserById() {
        UserResponse response = new UserResponse("john@example.com", "john", "John Doe", user.getRoleType(), LocalDateTime.now());
        when(repository.findById(1)).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(response);

        assertThat(service.findById(1)).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldResolveUsernamesByIds() {
        User second = User.builder().id(2).username("mary").build();
        when(repository.findAllById(Set.of(1, 2))).thenReturn(List.of(user, second));

        assertThat(service.findUsernamesByIds(Set.of(1, 2)))
                .isEqualTo(Map.of(1, "john", 2, "mary"));
    }

    @Test
    void shouldResolveSingleUsername() {
        when(repository.findById(1)).thenReturn(Optional.of(user));
        assertThat(service.findUsernamesById(1)).isEqualTo("john");
    }

    @Test
    void shouldThrowWhenResolvingMissingUsername() {
        when(repository.findById(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findUsernamesById(1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldFindIdsByUsernameFragment() {
        when(repository.findIdsByUsernameContaining("jo")).thenReturn(List.of(1, 3));
        assertThat(service.findIdsByUsernameContaining("jo")).containsExactly(1, 3);
    }
}
