package org.nemesiscodex.transfers.naive.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@Table("naive_balance")
public record NaiveBalance(
    @Id
    UUID id,
    UUID userId,
    BigDecimal amount,
    UUID openLedgerId,
    UUID closeLedgerId,
    Instant createdAt,
    Instant updatedAt
) {
    public static NaiveBalance from(UUID userId, BigDecimal amount) {
        return NaiveBalance.builder()
            .userId(userId)
            .amount(amount)
            .openLedgerId(UUID.randomUUID())
            .build();
    }

    public NaiveBalance close(UUID closeLedgerId) {
        return this.toBuilder()
            .closeLedgerId(closeLedgerId)
            .build();
    }
}
