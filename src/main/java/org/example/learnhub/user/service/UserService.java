package org.example.learnhub.user.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.user.repository.UserRepository;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.dto.UserRegisterRequest;
import org.example.learnhub.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;

    public UserResponse register(UserRegisterRequest request) {
        if (repository.existsByEmail(request.email())) throw new RuntimeException("Email is already in use.");
        if (repository.existsByUsername(request.username())) throw new RuntimeException("Username is already in use.");

        User user = mapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.password()));

        repository.save(user);

        return mapper.toDto(user);
    }

    public UserResponse findById(Integer id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapper.toDto(user);
    }
}
