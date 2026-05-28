package org.example.learnhub.user.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.config.TokenService;
import org.example.learnhub.exception.EmailAlreadyInUse;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.exception.UsernameAlreadyInUse;
import org.example.learnhub.user.dto.TokenResponse;
import org.example.learnhub.user.dto.UserLoginRequest;
import org.example.learnhub.user.repository.UserRepository;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.dto.UserRegisterRequest;
import org.example.learnhub.user.dto.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public TokenResponse register(UserRegisterRequest request) {
        if (repository.existsByEmail(request.email())) throw new EmailAlreadyInUse("Email is already in use.");
        if (repository.existsByUsername(request.username())) throw new UsernameAlreadyInUse("Username is already in use.");

        User user = mapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.password()));

        repository.save(user);

        return login(new UserLoginRequest(request.email(), request.password()));
    }

    public UserResponse findById(Integer id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFound("User not found"));

        return mapper.toDto(user);
    }

    public TokenResponse login(UserLoginRequest request) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(
                request.identifier(), request.password()
        );

        var auth = authenticationManager.authenticate(usernamePassword);
        var token = tokenService.generateToken((User) auth.getPrincipal());

        return new TokenResponse(token);
    }
}
