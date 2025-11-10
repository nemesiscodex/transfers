package org.nemesiscodex.transfers.core.entity;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@Table("users")
public record User(
    @Id
    UUID id,
    String username,
    @Column("password_hash")
    String passwordHash,
    String email,
    @Column("created_at")
    Instant createdAt,
    @Column("updated_at")
    Instant updatedAt
) {
}
