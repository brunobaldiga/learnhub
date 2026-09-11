package org.example.learnhub.user.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.user.dto.UserRegisterRequest;
import org.example.learnhub.user.dto.UserResponse;
import org.example.learnhub.user.entity.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    public User toUser(UserRegisterRequest request) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .fullName(request.fullName())
                .build();
    }

    public UserResponse toDto(User user) {
        return new UserResponse(
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getRoleType(),
                user.getCreatedAt()
        );
    }
}
