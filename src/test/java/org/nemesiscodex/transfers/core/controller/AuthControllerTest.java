package org.nemesiscodex.transfers.core.controller;

import org.junit.jupiter.api.Test;
import org.nemesiscodex.transfers.core.dto.LoginRequest;
import org.nemesiscodex.transfers.core.dto.SignupRequest;
import org.nemesiscodex.transfers.core.entity.User;
import org.nemesiscodex.transfers.core.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration;
import org.nemesiscodex.transfers.config.SecurityConfig;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        ReactiveWebSecurityAutoConfiguration.class,
        ReactiveUserDetailsServiceAutoConfiguration.class
    }
)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AuthService authService;

    @Test
    void shouldSignupUser() throws Exception {
        // Given
        SignupRequest request = new SignupRequest("testuser", "password123", "test@example.com");
        User user = new User(
            UUID.randomUUID(),
            "testuser",
            "hashedPassword",
            "test@example.com",
            Instant.now(),
            Instant.now()
        );

        when(authService.signup(any(SignupRequest.class))).thenReturn(Mono.just(user));

        // When/Then
        webTestClient.post()
            .uri("/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.username").isEqualTo("testuser")
            .jsonPath("$.email").isEqualTo("test@example.com")
            .jsonPath("$.passwordHash").doesNotExist();
    }

    @Test
    void shouldRejectSignupWithDuplicateUsername() {
        // Given
        SignupRequest request = new SignupRequest("existinguser", "password123", "test@example.com");

        when(authService.signup(any(SignupRequest.class)))
            .thenReturn(Mono.error(new IllegalStateException("Username already exists")));

        // When/Then
        webTestClient.post()
            .uri("/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isEqualTo(409);
    }

    @Test
    void shouldRejectSignupWithInvalidData() {
        // Given
        SignupRequest request = new SignupRequest("", "short", "invalid-email");

        // When/Then
        webTestClient.post()
            .uri("/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void shouldLoginWithValidCredentials() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        User user = new User(
            UUID.randomUUID(),
            "testuser",
            "hashedPassword",
            "test@example.com",
            Instant.now(),
            Instant.now()
        );
        AuthService.AuthenticationResult result = new AuthService.AuthenticationResult(user, "test-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(Mono.just(result));

        // When/Then
        webTestClient.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.token").isEqualTo("test-token")
            .jsonPath("$.user.username").isEqualTo("testuser")
            .jsonPath("$.user.email").isEqualTo("test@example.com");
    }

    @Test
    void shouldRejectLoginWithInvalidCredentials() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        when(authService.login(any(LoginRequest.class)))
            .thenReturn(Mono.error(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials")));

        // When/Then
        webTestClient.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void shouldRejectLoginWithInvalidData() {
        // Given
        LoginRequest request = new LoginRequest("", "");

        // When/Then
        webTestClient.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest();
    }
}
