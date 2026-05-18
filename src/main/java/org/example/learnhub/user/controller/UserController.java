package org.example.learnhub.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.dto.TokenResponse;
import org.example.learnhub.user.dto.UserLoginRequest;
import org.example.learnhub.user.dto.UserRegisterRequest;
import org.example.learnhub.user.dto.UserResponse;
import org.example.learnhub.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;


    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(
            @RequestBody @Validated UserRegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Validated UserLoginRequest request
    ) {

        return ResponseEntity.ok(service.login(request));
    }

    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.findById(user.getId()));
    }
}
