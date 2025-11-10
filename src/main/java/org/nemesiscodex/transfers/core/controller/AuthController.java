package org.nemesiscodex.transfers.core.controller;

import jakarta.validation.Valid;
import org.nemesiscodex.transfers.core.dto.LoginRequest;
import org.nemesiscodex.transfers.core.dto.LoginResponse;
import org.nemesiscodex.transfers.core.dto.SignupRequest;
import org.nemesiscodex.transfers.core.dto.UserResponse;
import org.nemesiscodex.transfers.core.entity.User;
import org.nemesiscodex.transfers.core.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<UserResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return this.authService.signup(request)
            .map(this::toUserResponse)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
            .onErrorMap(IllegalStateException.class, ex ->
                new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex)
            );
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return this.authService.login(request)
            .map(result -> new LoginResponse(result.token(), toUserResponse(result.user())))
            .map(ResponseEntity::ok)
            .onErrorMap(BadCredentialsException.class, ex ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex)
            );
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.id(),
            user.username(),
            user.email(),
            user.createdAt(),
            user.updatedAt()
        );
    }
}
