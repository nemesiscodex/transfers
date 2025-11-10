package org.nemesiscodex.transfers.core.controller;

import org.junit.jupiter.api.Test;
import org.nemesiscodex.transfers.core.entity.User;
import org.nemesiscodex.transfers.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration;
import org.nemesiscodex.transfers.config.SecurityConfig;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.core.userdetails.User.withUsername;

@WebFluxTest(
    controllers = UserController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        ReactiveWebSecurityAutoConfiguration.class,
        ReactiveUserDetailsServiceAutoConfiguration.class
    }
)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ReactiveUserDetailsService reactiveUserDetailsService;

    @Test
    void shouldReturnCurrentUserWhenAuthenticated() {
        // Given
        String username = "testuser";
        User testUser = new User(
            UUID.randomUUID(),
            username,
            "hashedPassword",
            "test@example.com",
            Instant.now(),
            Instant.now()
        );
        UserDetails userDetails = withUsername(username)
            .password("hashedPassword")
            .roles("USER")
            .build();

        when(userRepository.findByUsername(username)).thenReturn(Mono.just(testUser));

        // When/Then
        webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockUser(userDetails))
            .get()
            .uri("/user")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.username").isEqualTo(username)
            .jsonPath("$.email").isEqualTo("test@example.com")
            .jsonPath("$.passwordHash").doesNotExist();
    }

    @Test
    void shouldRejectUnauthenticatedRequest() {
        // When/Then
        webTestClient
            .get()
            .uri("/user")
            .exchange()
            .expectStatus().isUnauthorized();
    }
}
