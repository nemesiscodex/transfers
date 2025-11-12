package org.nemesiscodex.transfers.naive.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@Table("naive_transfer")
public record NaiveTransfer(
    @Id
    UUID id,
    UUID userId,
    UUID recipientId,
    BigDecimal amount,
    Instant createdAt,
    Instant updatedAt
) {
    public static NaiveTransfer from(UUID userId, UUID recipientId, BigDecimal amount) {
        return NaiveTransfer.builder()
            .userId(userId)
            .recipientId(recipientId)
            .amount(amount)
            .build();
    }
}
