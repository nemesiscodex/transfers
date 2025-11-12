package org.nemesiscodex.transfers.naive.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@Table("naive_ledger")
public record NaiveLedger(
    @Id
    UUID id,
    UUID userId,
    BigDecimal amount,
    UUID transferId,
    Instant createdAt,
    Instant updatedAt
) {
    public static NaiveLedger from(UUID userId, BigDecimal amount, UUID transferId) {
        return NaiveLedger.builder()
            .userId(userId)
            .amount(amount)
            .transferId(transferId)
            .build();
    }
}
