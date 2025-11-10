package org.nemesiscodex.transfers.core.dto;

public record LoginResponse(
    String token,
    UserResponse user) {
}

