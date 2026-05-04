package org.example.learnhub.user;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.config.TokenService;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.dto.TokenResponse;
import org.example.learnhub.user.dto.UserLoginRequest;
import org.example.learnhub.user.dto.UserRegisterRequest;
import org.example.learnhub.user.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestBody @Validated UserRegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Validated UserLoginRequest request
    ) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(
                request.identifier(), request.password()
        );

        var auth = authenticationManager.authenticate(usernamePassword);
        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.findById(user.getId()));
    }
}
