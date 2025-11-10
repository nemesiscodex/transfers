package org.nemesiscodex.transfers.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nemesiscodex.transfers.core.dto.LoginRequest;
import org.nemesiscodex.transfers.core.dto.SignupRequest;
import org.nemesiscodex.transfers.core.entity.User;
import org.nemesiscodex.transfers.core.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private AuthService authService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = mock(JwtService.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldSignupUser() {
        // Given
        String rawPassword = "password123";

        String hashedPassword = passwordEncoder.encode(rawPassword);
        User newUser = User.builder()
        .id(UUID.randomUUID())
        .username("testuser")
        .passwordHash(hashedPassword)
        .email("test@example.com")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
        SignupRequest request = new SignupRequest(newUser.username(), rawPassword, newUser.email());

        when(userRepository.existsByUsername("testuser")).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail("test@example.com")).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class)))
            .thenReturn(Mono.just(newUser));
        when(userRepository.findById(newUser.id()))
            .thenReturn(Mono.just(newUser));

        // When/Then
        StepVerifier.create(authService.signup(request))
            .assertNext(user -> {
                assertThat(user.username()).isEqualTo("testuser");
                assertThat(user.email()).isEqualTo("test@example.com");
                assertThat(user.passwordHash()).isNotEqualTo("password123"); // Should be hashed
                assertThat(passwordEncoder.matches("password123", user.passwordHash())).isTrue();
            })
            .verifyComplete();

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateUsername() {
        // Given
        SignupRequest request = new SignupRequest("existinguser", "password123", "test@example.com");

        when(userRepository.existsByUsername("existinguser")).thenReturn(Mono.just(true));

        // When/Then
        StepVerifier.create(authService.signup(request))
            .expectErrorMatches(ex -> ex instanceof IllegalStateException
                && ex.getMessage().equals("Username already exists"))
            .verify();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        // Given
        SignupRequest request = new SignupRequest("newuser", "password123", "existing@example.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(Mono.just(true));

        // When/Then
        StepVerifier.create(authService.signup(request))
            .expectErrorMatches(ex -> ex instanceof IllegalStateException
                && ex.getMessage().equals("Email already exists"))
            .verify();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginWithValidCredentials() {
        // Given
        String password = "password123";
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(
            UUID.randomUUID(),
            "testuser",
            hashedPassword,
            "test@example.com",
            Instant.now(),
            Instant.now()
        );
        LoginRequest request = new LoginRequest("testuser", password);
        String token = "test-jwt-token";

        when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(user));
        when(jwtService.generateToken(user.id(), user.username())).thenReturn(token);

        // When/Then
        StepVerifier.create(authService.login(request))
            .assertNext(result -> {
                assertThat(result.user()).isEqualTo(user);
                assertThat(result.token()).isEqualTo(token);
            })
            .verifyComplete();

        verify(jwtService).generateToken(user.id(), user.username());
    }

    @Test
    void shouldRejectLoginWithInvalidUsername() {
        // Given
        LoginRequest request = new LoginRequest("nonexistent", "password123");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Mono.empty());

        // When/Then
        StepVerifier.create(authService.login(request))
            .expectError(BadCredentialsException.class)
            .verify();

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void shouldRejectLoginWithInvalidPassword() {
        // Given
        String password = "password123";
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(
            UUID.randomUUID(),
            "testuser",
            hashedPassword,
            "test@example.com",
            Instant.now(),
            Instant.now()
        );
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(user));

        // When/Then
        StepVerifier.create(authService.login(request))
            .expectError(BadCredentialsException.class)
            .verify();

        verify(jwtService, never()).generateToken(any(), any());
    }
}

