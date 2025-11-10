package org.nemesiscodex.transfers.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "testSecretKeyForJwtServiceTestingPurposesOnly123456";
    private static final long TEST_EXPIRATION_HOURS = 24;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, TEST_EXPIRATION_HOURS);
    }

    @Test
    void shouldGenerateToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String username = "testuser";

        // When
        String token = jwtService.generateToken(userId, username);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        String token = jwtService.generateToken(userId, username);

        // When/Then
        StepVerifier.create(jwtService.extractUsername(token))
            .expectNext(username)
            .verifyComplete();
    }

    @Test
    void shouldValidateToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        String token = jwtService.generateToken(userId, username);

        // When
        boolean isValid = jwtService.isTokenValid(token, username);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";
        String username = "testuser";

        // When
        boolean isValid = jwtService.isTokenValid(invalidToken, username);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectTokenWithWrongUsername() {
        // Given
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        String token = jwtService.generateToken(userId, username);

        // When
        boolean isValid = jwtService.isTokenValid(token, "differentuser");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectExtractionFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When/Then
        StepVerifier.create(jwtService.extractUsername(invalidToken))
            .expectError(IllegalArgumentException.class)
            .verify();
    }
}

