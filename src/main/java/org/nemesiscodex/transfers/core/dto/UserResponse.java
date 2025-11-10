package org.nemesiscodex.transfers.core.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String email,
    Instant createdAt,
    Instant updatedAt) {
}

